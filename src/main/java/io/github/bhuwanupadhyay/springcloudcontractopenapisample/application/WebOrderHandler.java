package io.github.bhuwanupadhyay.springcloudcontractopenapisample.application;

import io.github.bhuwanupadhyay.springcloudcontractopenapisample.domain.OrderEntity;
import io.github.bhuwanupadhyay.springcloudcontractopenapisample.domain.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.print.Pageable;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
class WebOrderHandler implements OrderHandler {

    private final AtomicInteger orderIdGen = new AtomicInteger(1000);
    private OrderRepository orderRepository;

    @Override
    public Mono<OrderResource> createOrder(CreateOrderCommand command) {
        return Mono.justOrEmpty(
                toResource(
                        orderRepository.save(
                                new OrderEntity(
                                        String.valueOf(orderIdGen.incrementAndGet()),
                                        command.customerId(),
                                        command.itemName(),
                                        command.quantity()
                                )
                        )
                )
        );
    }

    @Override
    public Mono<ResponseEntity<OrderResource>> updateOrder(String orderId,
                                                           UpdateOrderCommand command) {
        ResponseEntity<OrderResource> result = orderRepository.find(orderId)
                .map(entity -> orderRepository.update(orderId, toEntity(command, entity)))
                .map(entity -> ResponseEntity.status(HttpStatus.OK).body(toResource(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        return Mono.justOrEmpty(result);
    }

    @Override
    public Flux<OrderResource> listOrder(OrderResource filters, Pageable pageable) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<OrderResource>> getOrder(String orderId) {
        ResponseEntity<OrderResource> result = orderRepository.find(orderId)
                .map(entity -> ResponseEntity.status(HttpStatus.OK).body(toResource(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        return Mono.justOrEmpty(result);
    }

    @Override
    public Mono<ResponseEntity<Object>> deleteOrder(String orderId) {
        return Mono.justOrEmpty(
                orderRepository.find(orderId)
                        .map(entity -> {
                            orderRepository.delete(orderId);
                            return ResponseEntity.status(HttpStatus.OK).build();
                        })
                        .orElseGet(() -> ResponseEntity.notFound().build())
        );
    }

    private OrderResource toResource(OrderEntity entity) {
        return new OrderResource(
                entity.getOrderId(),
                entity.getCustomerId(),
                entity.getItemName(),
                entity.getQuantity()
        );
    }

    private OrderEntity toEntity(UpdateOrderCommand command, OrderEntity entity) {
        return new OrderEntity(entity.getOrderId(), entity.getCustomerId(), command.itemName(), command.quantity());
    }

}