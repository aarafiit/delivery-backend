package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.dto.AddressesDTO;
import kn.org.deliverybackend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/app/consumer")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<List<AddressesDTO>> getAddresses(@PathVariable UUID userId) {
        List<AddressesDTO> addresses = addressService.getAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/{userId}/addresses")
    public ResponseEntity<AddressesDTO> addAddress(
            @PathVariable UUID userId,
            @RequestBody AddressesDTO addressesDTO) {
        AddressesDTO createdAddress = addressService.addAddress(userId, addressesDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
    }

    @PutMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<AddressesDTO> updateAddress(
            @PathVariable UUID userId,
            @PathVariable Long addressId,
            @RequestBody AddressesDTO addressesDTO) {
        AddressesDTO updatedAddress = addressService.updateAddress(userId, addressId, addressesDTO);
        return ResponseEntity.ok(updatedAddress);
    }

    @GetMapping("/{userId}/address/{addressId}")
    public ResponseEntity<AddressesDTO> getAddress(
            @PathVariable UUID userId,
            @PathVariable Long addressId) {
       return ResponseEntity.ok(addressService.getAddressById(userId, addressId));
    }

    @DeleteMapping("/{userId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID userId,
            @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
