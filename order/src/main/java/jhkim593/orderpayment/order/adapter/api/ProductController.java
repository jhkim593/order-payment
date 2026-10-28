package jhkim593.orderpayment.order.adapter.api;

import jhkim593.orderpayment.order.application.provided.ProductUpdater;
import jhkim593.orderpayment.order.domain.dto.RestockRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductUpdater productUpdater;

    @PostMapping("/api/v1/products/{productId}/restock")
    public ResponseEntity<Void> restock(@PathVariable Long productId, @RequestBody RestockRequestDto request) {
        productUpdater.restock(productId, request.getQuantity());
        return ResponseEntity.ok().build();
    }
}