package com.playdata.orderingservice.ordering.dto;

import com.playdata.orderingservice.common.auth.Role;
import com.playdata.orderingservice.common.entity.Address;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResDto {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private Address address;

    private boolean dummyFlag;

}
