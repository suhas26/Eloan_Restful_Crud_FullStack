package com.iiht.training.eloan.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UsersRepository usersRepository;
	
	private Users covertInputDtoToEntity(UserDto userDto, String role) {
		Users user = new Users();
		
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setEmail(userDto.getEmail());
		user.setMobile(userDto.getMobile());
		user.setRole(role);
		
		return user;
	}
	
	private UserDto convertEntityToOutputDto(Users user) {
		UserDto userDto = new UserDto();
		
		userDto.setFirstName(user.getFirstName());
		userDto.setLastName(user.getLastName());
		userDto.setEmail(user.getEmail());
		userDto.setMobile(user.getMobile());
		userDto.setId(user.getId());
		
		return userDto;
	}
	
	
	@Override
	public UserDto registerClerk(UserDto userDto) {
		// convert DTO into entity
		Users user = this.covertInputDtoToEntity(userDto, "Clerk");
		
		// save entity in DB : returns the copy of newly added record back
		Users newUser = this.usersRepository.save(user);
		
		// convert entity into output DTO
		UserDto newUserDto = this.convertEntityToOutputDto(newUser);
		
		return newUserDto;
	}

	@Override
	public UserDto registerManager(UserDto userDto) {
		// convert DTO into entity
		Users user = this.covertInputDtoToEntity(userDto, "Manager");
		
		// save entity in DB : returns the copy of newly added record back
		Users newUser = this.usersRepository.save(user);
		
		// convert entity into output DTO
		UserDto newUserDto = this.convertEntityToOutputDto(newUser);
		
		return newUserDto;
	}

	@Override
	public List<UserDto> getAllClerks() {
		List<Users> allUsers = this.usersRepository.findAllByRole("Clerk").orElse(null);
		
		List<UserDto> allClerks = allUsers.stream().map(this::convertEntityToOutputDto).
				collect(Collectors.toList());
		
		return allClerks;
	}

	@Override
	public List<UserDto> getAllManagers() {
		List<Users> allUsers = this.usersRepository.findAllByRole("Manager").orElse(null);
		
		List<UserDto> allManagers = allUsers.stream().map(this::convertEntityToOutputDto).
				collect(Collectors.toList());
		
		return allManagers;
	}

}
