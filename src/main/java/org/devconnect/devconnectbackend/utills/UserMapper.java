package org.devconnect.devconnectbackend.utills;

import org.devconnect.devconnectbackend.dto.UserRegistrationDTO;
import org.devconnect.devconnectbackend.dto.UserResponseDTO;
import org.devconnect.devconnectbackend.dto.UserUpdateDTO;
import org.devconnect.devconnectbackend.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        } else {
            UserResponseDTO userResponseDTO = new UserResponseDTO();

            userResponseDTO.setUserId(user.getUserId());
            userResponseDTO.setUsername(user.getUsername());
            userResponseDTO.setFirstName(user.getFirstName());
            userResponseDTO.setLastName(user.getLastName());
            userResponseDTO.setEmail(user.getEmail());
            userResponseDTO.setTelephone(user.getTelephone());
            userResponseDTO.setUserRole(user.getUserRole());
            userResponseDTO.setUserStatus(user.getUserStatus());
            userResponseDTO.setCreatedAt(user.getCreatedAt());
            userResponseDTO.setLastSeen(user.getLastSeen());
            userResponseDTO.setActive(user.isActive());

            return userResponseDTO;
        }
    }

    public User toUserModel(UserRegistrationDTO userRegistrationDTO) {
        if (userRegistrationDTO == null) {
            return null;
        } else {
            User userModel = new User();

            userModel.setUsername(userRegistrationDTO.getUsername());
            userModel.setFirstName(userRegistrationDTO.getFirstName());
            userModel.setLastName(userRegistrationDTO.getLastName());
            userModel.setEmail(userRegistrationDTO.getEmail());
            userModel.setTelephone(userRegistrationDTO.getTelephone());
            userModel.setUserRole(userRegistrationDTO.getUserRole());

            return userModel;
        }
    }

    public void updateUserFromDTO(UserUpdateDTO userUpdateDTO, User userModel) {
        if (userUpdateDTO != null && userModel != null) {
            if (userUpdateDTO.getUsername() != null) {
                userModel.setUsername(userUpdateDTO.getUsername());
            }
            if (userUpdateDTO.getFirstName() != null) {
                userModel.setFirstName(userUpdateDTO.getFirstName());
            }
            if (userUpdateDTO.getLastName() != null) {
                userModel.setLastName(userUpdateDTO.getLastName());
            }
            if (userUpdateDTO.getEmail() != null) {
                userModel.setEmail(userUpdateDTO.getEmail());
            }
            if (userUpdateDTO.getTelephone() != null) {
                userModel.setTelephone(userUpdateDTO.getTelephone());
            }
        }
    }
}
