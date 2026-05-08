package kn.org.deliverybackend.service;

import kn.org.deliverybackend.dto.AddressesDTO;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    List<AddressesDTO> getAddresses(UUID userId);

    AddressesDTO addAddress(UUID userId, AddressesDTO addressesDTO);

    AddressesDTO updateAddress(UUID userId, Long addressId, AddressesDTO addressesDTO);

    AddressesDTO getAddressById(UUID userId, Long addressId);

    void deleteAddress(UUID userId, Long addressId);
}
