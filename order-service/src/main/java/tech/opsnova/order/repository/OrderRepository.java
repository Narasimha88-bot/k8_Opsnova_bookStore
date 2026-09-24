package tech.opsnova.order.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import tech.opsnova.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByUsernameOrderByCreatedAtDesc(String username);
}
