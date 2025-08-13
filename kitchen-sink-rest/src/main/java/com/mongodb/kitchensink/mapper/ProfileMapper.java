package com.mongodb.kitchensink.mapper;

import com.mongodb.kitchensink.dto.AddressRequest;
import com.mongodb.kitchensink.dto.RegistrationRequest;
import com.mongodb.kitchensink.model.Address;
import com.mongodb.kitchensink.model.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(source = "street", target = "street")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "pincode", target = "pincode")
    @Mapping(source = "country", target = "country")
    Address toAddress(AddressRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Profile toProfile(RegistrationRequest request);
}
