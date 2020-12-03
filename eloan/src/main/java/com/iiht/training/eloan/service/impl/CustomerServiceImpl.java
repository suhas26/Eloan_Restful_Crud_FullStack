package com.iiht.training.eloan.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.LoanDto;
import com.iiht.training.eloan.dto.LoanOutputDto;
import com.iiht.training.eloan.dto.ProcessingDto;
import com.iiht.training.eloan.dto.SanctionOutputDto;
import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Loan;
import com.iiht.training.eloan.entity.ProcessingInfo;
import com.iiht.training.eloan.entity.SanctionInfo;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.exception.CustomerNotFoundException;
import com.iiht.training.eloan.exception.LoanNotFoundException;
import com.iiht.training.eloan.repository.LoanRepository;
import com.iiht.training.eloan.repository.ProcessingInfoRepository;
import com.iiht.training.eloan.repository.SanctionInfoRepository;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository pProcessingInfoRepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;
	
	private Users covertCustomerInputDtoToEntity(UserDto userDto, String role) {
		Users user = new Users();
		
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setEmail(userDto.getEmail());
		user.setMobile(userDto.getMobile());
		user.setRole(role);
		
		return user;
	}
	
	private UserDto convertCustomerEntityToOutputDto(Users user) {
		UserDto userDto = new UserDto();
		
		userDto.setFirstName(user.getFirstName());
		userDto.setLastName(user.getLastName());
		userDto.setEmail(user.getEmail());
		userDto.setMobile(user.getMobile());
		userDto.setId(user.getId());
		
		return userDto;
	}
	
	private Loan convertLoanDtoToEntity(Long customerId, LoanDto loanDto, Integer status, String remark) {
		Loan newLoan = new Loan();
		
		newLoan.setCustomerId(customerId);
		newLoan.setLoanName(loanDto.getLoanName());
		newLoan.setLoanAmount(loanDto.getLoanAmount());
		newLoan.setLoanApplicationDate(loanDto.getLoanApplicationDate());
		newLoan.setBusinessStructure(loanDto.getBusinessStructure());
		newLoan.setBillingIndicator(loanDto.getBillingIndicator());
		newLoan.setTaxIndicator(loanDto.getTaxIndicator());
		newLoan.setStatus(status);
		newLoan.setRemark(remark);
		
		return newLoan;
	}
	
	private LoanDto convertEntityToLoanDto(Loan loan) {
		LoanDto loanDto = new LoanDto();
		
		loanDto.setLoanName(loan.getLoanName());
		loanDto.setLoanAmount(loan.getLoanAmount());
		loanDto.setLoanApplicationDate(loan.getLoanApplicationDate());
		loanDto.setBusinessStructure(loan.getBusinessStructure());
		loanDto.setBillingIndicator(loan.getBillingIndicator());
		loanDto.setTaxIndicator(loan.getTaxIndicator());
		
		return loanDto;
	}
	
	private LoanOutputDto convertLoanEntityToOutputDto(Loan loan, UserDto userDto, LoanDto loanDto, ProcessingDto processingDto,
			SanctionOutputDto sanctionOutputDto) {
		LoanOutputDto loanOutputDto = new LoanOutputDto();
		String status;
		
		loanOutputDto.setLoanAppId(loan.getId());
		loanOutputDto.setCustomerId(loan.getCustomerId());
		loanOutputDto.setUserDto(userDto);
		loanOutputDto.setLoanDto(loanDto);
		loanOutputDto.setProcessingDto(processingDto);
		loanOutputDto.setSanctionOutputDto(sanctionOutputDto);
		if (loan.getStatus()==0) {
			status = "Applied";
		} else if(loan.getStatus()==1) {
			status = "Processed";
		} else if(loan.getStatus()==2) {
			status = "Sanctioned";
		} else if(loan.getStatus()==-1) {
			status = "Rejected";
		} else {
			status = "";
		}
		loanOutputDto.setStatus(status);
		loanOutputDto.setRemark(loan.getRemark());
		
		return loanOutputDto;
	}
	
	private ProcessingDto convertProcessingInfoToDto(ProcessingInfo processingInfo) {
		if (processingInfo!=null) {
			ProcessingDto processingDto = new ProcessingDto();
			
			processingDto.setAcresOfLand(processingInfo.getAcresOfLand());
			processingDto.setLandValue(processingInfo.getLandValue());
			processingDto.setAppraisedBy(processingInfo.getAppraisedBy());
			processingDto.setValuationDate(processingInfo.getValuationDate());
			processingDto.setAddressOfProperty(processingInfo.getAddressOfProperty());
			processingDto.setSuggestedAmountOfLoan(processingInfo.getSuggestedAmountOfLoan());
			
			return processingDto;
		} else {
			return null;
		}
	}
	
	private SanctionOutputDto convertSanctionInfoToOutputDto(SanctionInfo sanctionInfo) {
		if (sanctionInfo!=null) {
			SanctionOutputDto sanctionOutputDto = new SanctionOutputDto();
			
			sanctionOutputDto.setLoanAmountSanctioned(sanctionInfo.getLoanAmountSanctioned());
			sanctionOutputDto.setTermOfLoan(sanctionInfo.getTermOfLoan());
			sanctionOutputDto.setPaymentStartDate(sanctionInfo.getPaymentStartDate());
			sanctionOutputDto.setLoanClosureDate(sanctionInfo.getLoanClosureDate());
			sanctionOutputDto.setMonthlyPayment(sanctionInfo.getMonthlyPayment());
			
			return sanctionOutputDto;
		} else {
			return null;
		}
	}
	
	public Long getLoanAppId(Loan loan) {
		Long loanAppId = loan.getId();
		
		return loanAppId;
	}
	
	@Override
	public UserDto register(UserDto userDto) {
		// convert DTO into entity
		Users user = this.covertCustomerInputDtoToEntity(userDto, "Customer");
		
		// save entity in DB : returns the copy of newly added record back
		Users newUser = this.usersRepository.save(user);
		
		// convert entity into output DTO
		UserDto newUserDto = this.convertCustomerEntityToOutputDto(newUser);
		
		return newUserDto;
	}

	@Override
	public LoanOutputDto applyLoan(Long customerId, LoanDto loanDto){
		Users user = this.usersRepository.findByIdAndRole(customerId,"Customer").orElseThrow(() -> new CustomerNotFoundException("Customer Not Found"));
		// convert DTO into entity
		Loan loan = this.convertLoanDtoToEntity(customerId, loanDto, 0, "");
		
		Loan newLoan = this.loanRepository.save(loan);
		
		// convert entity into output DTO
		UserDto userDto = this.convertCustomerEntityToOutputDto(user);
		LoanOutputDto loanOutputDto = this.convertLoanEntityToOutputDto(newLoan, userDto, loanDto, null, null); 
		
		return loanOutputDto;
	}

	@Override
	public LoanOutputDto getStatus(Long loanAppId){
		
		Loan appliedLoan = this.loanRepository.findById(loanAppId).orElseThrow(() -> new LoanNotFoundException("Loan Not Found"));
		Users user = this.usersRepository.findById(appliedLoan.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException("Customer Not Found"));
		ProcessingInfo processingInfo = this.pProcessingInfoRepository.findByLoanAppId(appliedLoan.getId()).orElse(null);
		SanctionInfo sanctionInfo = this.sanctionInfoRepository.findByLoanAppId(appliedLoan.getId()).orElse(null);
		
		// convert entity into output DTO
		UserDto userDto = this.convertCustomerEntityToOutputDto(user);
		LoanDto loanDto = this.convertEntityToLoanDto(appliedLoan);
		ProcessingDto processingDto = this.convertProcessingInfoToDto(processingInfo);
		SanctionOutputDto sanctionOutputDto = this.convertSanctionInfoToOutputDto(sanctionInfo);
		LoanOutputDto loanOutputDto = this.convertLoanEntityToOutputDto(appliedLoan, userDto, loanDto, processingDto, sanctionOutputDto); 
		
		return loanOutputDto;
	}

	@Override
	public List<LoanOutputDto> getStatusAll(Long customerId) {
		this.usersRepository.findByIdAndRole(customerId,"Customer").orElseThrow(() -> new CustomerNotFoundException("Customer Not Found"));
		
		//Get List of loans using Customer Id
		List<Loan> allLoan = this.loanRepository.findAllByCustomerId(customerId).orElseThrow(() -> new LoanNotFoundException("No Applied Loans Found"));

		//Get List of application Id's using Loan object
		List<Long> loanAppIds = allLoan.stream().map(this::getLoanAppId).collect(Collectors.toList()); 
		
		//Get LoanOutputDto object from the application Id's 
		List<LoanOutputDto> loanOutputDto = loanAppIds.stream().map(this::getStatus).collect(Collectors.toList());
		
		return loanOutputDto;
	}

}
