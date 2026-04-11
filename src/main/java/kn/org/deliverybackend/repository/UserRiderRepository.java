package kn.org.deliverybackend.repository;

import kn.org.deliverybackend.entity.UserRider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRiderRepository extends JpaRepository<UserRider, UUID> {
}
