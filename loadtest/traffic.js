import http from 'k6/http';
import { sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8081';
const USER_ID = 1;
const PAYMENT_METHOD_IDS = [1, 2, 3];
const PRODUCTS = [
  { productId: 1, price: 10000 },
  { productId: 2, price: 5000 },
  { productId: 3, price: 30000 },
];
const JSON_HEADERS = { headers: { 'Content-Type': 'application/json' } };

export const options = {
  scenarios: {
    users: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 1),
      timeUnit: '1s',
      duration: '8760h',
      preAllocatedVUs: 10,
      maxVUs: 100,
    }, restock: {
      executor: 'constant-arrival-rate',
      exec: 'restock',
      rate: 1,
      timeUnit: '10m',
      duration: '8760h',
      preAllocatedVUs: 1,
    },
  },
};

// 초당 RATE명 × 주문 25% × 평균 1.5개 ÷ 상품 3개 ≈ 상품당 10분에 75×RATE개 소비 → 2배 입고
export function restock() {
  const quantity = 150 * Number(__ENV.RATE || 1);
  PRODUCTS.forEach((p) => http.post(`${BASE}/api/v1/products/${p.productId}/restock`,
    JSON.stringify({ quantity }), { ...JSON_HEADERS, tags: { name: 'restock' } }));
}

const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const json = (res, path) => (res.status === 200 ? res.json(path) : null);

function browse() {
  const res = http.get(`${BASE}/api/v1/orders?userId=${USER_ID}`, { tags: { name: 'orders' } });
  const orders = json(res, 'orders') || [];
  if (orders.length) {
    sleep(Math.random() * 2);
    http.get(`${BASE}/api/v1/orders/${pick(orders).orderId}`, { tags: { name: 'order-detail' } });
  }
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
  const orders = json(http.get(`${BASE}/api/v1/orders?userId=${USER_ID}`, { tags: { name: 'orders' } }), 'orders') || [];
  const target = orders.find((o) => o.status === 'SUCCEEDED');
  if (!target) return;
  const items = json(http.get(`${BASE}/api/v1/orders/${target.orderId}`, { tags: { name: 'order-detail' } }), 'items') || [];
  const item = items.find((i) => i.orderedQuantity > i.cancelledQuantity);
  if (!item) return;
  http.post(`${BASE}/api/v1/orders/${target.orderId}/cancel`, JSON.stringify({
    reason: '단순 변심',
    items: [{ orderProductId: item.orderProductId, quantity: 1 }],
  }), { ...JSON_HEADERS, tags: { name: 'order-cancel' } });
}

export default function () {
  const r = Math.random();
  if (r < 0.7) browse();
  else if (r < 0.95) order();
  else cancel();
}
