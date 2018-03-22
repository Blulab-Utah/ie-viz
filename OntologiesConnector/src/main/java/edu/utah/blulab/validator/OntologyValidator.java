package edu.utah.blulab.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import edu.utah.blulab.model.OntologyModel;

@Component
public class OntologyValidator implements Validator {

	public boolean supports(Class clazz) {
		return OntologyModel.class.isAssignableFrom(clazz);
	}

	public void validate(Object target, Errors errors) 
	{
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "error.firstName", "First name is required.");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "error.lastName", "Last name is required.");
//		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "error.email", "Email is required.");
	}

}
