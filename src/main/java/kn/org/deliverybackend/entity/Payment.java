package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends AbstractBaseEntity<UUID> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID orderId;

    private String method;

    private String transactionId;

    private BigDecimal amount;

    private String status;
}
