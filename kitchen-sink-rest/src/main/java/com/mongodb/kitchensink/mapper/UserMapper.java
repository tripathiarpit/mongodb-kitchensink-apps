package com.mongodb.kitchensink.mapper;

import com.mongodb.kitchensink.dto.ProfileDto;
import com.mongodb.kitchensink.dto.UserDto;
import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "profile", ignore = true)
    @Mapping(source = "accountVerificationPending", target = "accountVerificationPending")
    @Mapping(source = "firstLogin", target = "firstLogin")
    UserDto toDto(User user);

    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    @Mapping(source = "address.country", target = "country")
    @Mapping(source = "address.pincode", target = "pincode")
    ProfileDto toDto(Profile profile);

    User toEntity(UserDto userDto);
}