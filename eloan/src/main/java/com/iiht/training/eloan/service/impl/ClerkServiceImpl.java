package com.iiht.training.eloan.service.impl;

import java.util.ArrayList;
import java.util.List;

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
import com.iiht.training.eloan.exception.AlreadyProcessedException;
import com.iiht.training.eloan.exception.ClerkNotFoundException;
import com.iiht.training.eloan.exception.CustomerNotFoundException;
import com.iiht.training.eloan.exception.LoanNotFoundException;
import com.iiht.training.eloan.repository.LoanRepository;
import com.iiht.training.eloan.repository.ProcessingInfoRepository;
import com.iiht.training.eloan.repository.SanctionInfoRepository;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.ClerkService;
import com.iiht.training.eloan.service.CustomerService;

@Service
public class ClerkServiceImpl implements ClerkService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository pProcessingInfoRepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;
	
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
	
	private UserDto convertUserEntityToDto(Users user) {
		UserDto userDto = new UserDto();
		
		userDto.setFirstName(user.getFirstName());
		userDto.setLastName(user.getLastName());
		userDto.setEmail(user.getEmail());
		userDto.setMobile(user.getMobile());
		userDto.setId(user.getId());
		
		return userDto;
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
	
	private ProcessingInfo convertLoanDtoToEntity(Long clerkId, Long loanAppId, ProcessingDto processingDto) {
		ProcessingInfo processingInfo = new ProcessingInfo();
		
		processingInfo.setLoanClerkId(clerkId);
		processingInfo.setLoanAppId(loanAppId);
		processingInfo.setAcresOfLand(processingDto.getAcresOfLand());
		processingInfo.setLandValue(processingDto.getLandValue());
		processingInfo.setAppraisedBy(processingDto.getAppraisedBy());
		processingInfo.setValuationDate(processingDto.getValuationDate());
		processingInfo.setAddressOfProperty(processingDto.getAddressOfProperty());
		processingInfo.setSuggestedAmountOfLoan(processingDto.getSuggestedAmountOfLoan());
		
		return processingInfo;
	}
	
	private ProcessingDto convertEntityToLoanDto (ProcessingInfo processingInfo) {
		ProcessingDto processingDto = new ProcessingDto();
		
		processingDto.setAcresOfLand(processingInfo.getAcresOfLand());
		processingDto.setLandValue(processingInfo.getLandValue());
		processingDto.setAppraisedBy(processingInfo.getAppraisedBy());
		processingDto.setValuationDate(processingInfo.getValuationDate());
		processingDto.setAddressOfProperty(processingInfo.getAddressOfProperty());
		processingDto.setSuggestedAmountOfLoan(processingInfo.getSuggestedAmountOfLoan());
		
		return processingDto;
	}
	
	@Override
	public List<LoanOutputDto> allAppliedLoans() {
		List<Loan> allAppliedLoans = this.loanRepository.findAllByStatus(0).orElseThrow(() -> new LoanNotFoundException("No applied Loans Found"));
		List<LoanOutputDto> allLoanOutputDto = new ArrayList<LoanOutputDto>();
		
		for (Loan appliedLoan:allAppliedLoans) {
			Users user = this.usersRepository.findById(appliedLoan.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException("Customer not Found"));
			
			// convert entity into output DTO
			UserDto userDto = this.convertUserEntityToDto(user);
			LoanDto loanDto = this.convertEntityToLoanDto(appliedLoan);
			LoanOutputDto loanOutputDto = this.convertLoanEntityToOutputDto(appliedLoan, userDto, loanDto, null, null); 
			allLoanOutputDto.add(loanOutputDto);
		}
		
		return allLoanOutputDto;
	}

	@Override
	public ProcessingDto processLoan(Long clerkId, Long loanAppId, ProcessingDto processingDto) {
		this.usersRepository.findByIdAndRole(clerkId,"Clerk").orElseThrow(() -> new ClerkNotFoundException("Clerk Not Found"));
		Loan loan = this.loanRepository.findById(loanAppId).orElseThrow(() -> new LoanNotFoundException("Loan Not Found"));
		if (loan.getStatus() != 0) {
			throw new AlreadyProcessedException("Already Processed Loan");
		}
		ProcessingInfo processingInfo = this.convertLoanDtoToEntity(clerkId, loanAppId, processingDto);
		
		ProcessingInfo newProcessingInfo = this.pProcessingInfoRepository.save(processingInfo);
		this.loanRepository.setStatusForLoan(1, loanAppId);
		
		ProcessingDto newProcessingDto = this.convertEntityToLoanDto(newProcessingInfo);
		
		return newProcessingDto;
	}

}
