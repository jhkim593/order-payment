import http from 'k6/http';

const BASE = __ENV.BASE_URL || 'http://localhost:8081';
const USER_ID = 1;
const PAYMENT_METHOD_IDS = [1, 2, 3];
const PRODUCTS = [
  { productId: 1, price: 10000 },
  { productId: 2, price: 5000 },
  { productId: 3, price: 30000 },
];
const READ_RPS = Number(__ENV.READ_RPS || 1000);
const WRITE_RPS = Number(__ENV.WRITE_RPS || 10);
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export const options = {
  scenarios: {
    reads: {
      executor: 'constant-arrival-rate',
      exec: 'read',
      rate: READ_RPS,
      timeUnit: '1s',
      duration: '8760h',
      preAllocatedVUs: 200,
      maxVUs: 2000,
    },
    writes: {
      executor: 'constant-arrival-rate',
      exec: 'write',
      rate: WRITE_RPS,
      timeUnit: '1s',
      duration: '8760h',
      preAllocatedVUs: 10,
      maxVUs: 100,
    },
    restock: {
      executor: 'constant-arrival-rate',
      exec: 'restock',
      rate: 1,
      timeUnit: '10m',
      duration: '8760h',
      preAllocatedVUs: 1,
    },
  },
};

// 초당 WRITE_RPS건 × 주문 5/6 × 평균 1.5개 ÷ 상품 3개 ≈ 상품당 10분에 250×WRITE_RPS개 소비 → 2배 입고
export function restock() {
  const quantity = 500 * WRITE_RPS;
  PRODUCTS.forEach((p) => http.post(`${BASE}/api/v1/products/${p.productId}/restock`,
    JSON.stringify({ quantity }), { ...JSON_HEADERS, tags: { name: 'restock' } }));
}

const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const json = (res, path) => (res.status === 200 ? res.json(path) : null);
const listOrders = () => json(http.get(`${BASE}/api/v1/orders?userId=${USER_ID}`, { tags: { name: 'orders' } }), 'orders') || [];

let orderIds = [];

export function read() {
  if (orderIds.length && Math.random() < 0.5) {
    http.get(`${BASE}/api/v1/orders/${pick(orderIds)}`, { tags: { name: 'order-detail' } });
    return;
  }
  const orders = listOrders();
  if (orders.length) orderIds = orders.map((o) => o.orderId);
}

function order() {
  const p = pick(PRODUCTS);
  http.post(`${BASE}/api/v1/orders`, JSON.stringify({
    userId: USER_ID,
    paymentMethodId: pick(PAYMENT_METHOD_IDS),
    items: [{ productId: p.productId, price: p.price, quantity: 1 + Math.floor(Math.random() * 2) }],
  }), { ...JSON_HEADERS, tags: { name: 'order-create' } });
}

function cancel() {
  const target = listOrders().find((o) => o.status === 'SUCCEEDED');
  if (!target) return;
  const items = json(http.get(`${BASE}/api/v1/orders/${target.orderId}`, { tags: { name: 'order-detail' } }), 'items') || [];
  const item = items.find((i) => i.orderedQuantity > i.cancelledQuantity);
  if (!item) return;
  http.post(`${BASE}/api/v1/orders/${target.orderId}/cancel`, JSON.stringify({
    reason: '단순 변심',
    items: [{ orderProductId: item.orderProductId, quantity: 1 }],
  }), { ...JSON_HEADERS, tags: { name: 'order-cancel' } });
}

export function write() {
  if (Math.random() < 5 / 6) order();
  else cancel();
}
