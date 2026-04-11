package kn.org.deliverybackend.entity;

import jakarta.persistence.*;
import kn.org.deliverybackend.entity.base.AbstractBaseEntity;
import lombok.*;

@Entity
@Table(name = "variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Variant extends AbstractBaseEntity<Long> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long productId;

    private Long categoryId;

    private Long quantity;

    private String name;

    private Long shopId;

}
