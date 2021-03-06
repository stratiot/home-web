package eu.daiad.web.controller.api;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.RoleUpdateRequest;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserRegistrationRequest;
import eu.daiad.web.model.user.UserRegistrationResponse;
import eu.daiad.web.security.PasswordChangeRequest;
import eu.daiad.web.service.IUserService;

@RestController("RestUserController")
public class UserController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(UserController.class);

	@Autowired
	private IUserService userService;

	@Autowired
	private org.springframework.validation.Validator validator;

	@Value("${security.white-list}")
	private boolean enforceWhiteListCheck;

	/**
	 * Creates a new user.
	 * 
	 * @param request the request.
	 * @param results the binding results.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/user/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse register(@RequestBody UserRegistrationRequest request, BindingResult results) {
		RestResponse response = new RestResponse();

		try {
			if (enforceWhiteListCheck) {
				((OptionalValidatorFactoryBean) validator).validate(request, results,
								Account.AccountSimpleValidation.class);
			} else {
				((OptionalValidatorFactoryBean) validator).validate(request.getAccount(), results,
								Account.AccountDefaultValidation.class);
			}

			if (results.hasErrors()) {
				for (FieldError e : results.getFieldErrors()) {
					response.add(this.getError(e));
				}

				return response;
			}

			UUID userKey = userService.createUser(request);

			UserRegistrationResponse registerResponse = new UserRegistrationResponse();
			registerResponse.setUserKey(userKey.toString());

			return registerResponse;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Changes a user's password.
	 * 
	 * @param data the request.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/user/password", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse changePassword(@RequestBody PasswordChangeRequest data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_USER);

			userService.setPassword(data.getCredentials().getUsername(), data.getPassword());

			return new RestResponse();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Grants a role to a user.
	 * 
	 * @param data the request.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/user/role/grant", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse addRole(@RequestBody RoleUpdateRequest data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			userService.setRole(data.getUsername(), data.getRole(), true);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Revokes a role to a user.
	 * 
	 * @param data the request.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/user/role/revoke", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse revokeRole(@RequestBody RoleUpdateRequest data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			userService.setRole(data.getUsername(), data.getRole(), false);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
