package eu.daiad.web.controller.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.domain.application.LogEventEntity;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.logging.LogEvent;
import eu.daiad.web.model.logging.LogEventQuery;
import eu.daiad.web.model.logging.LogEventQueryRequest;
import eu.daiad.web.model.logging.LogEventQueryResponse;
import eu.daiad.web.model.logging.LogEventQueryResult;
import eu.daiad.web.repository.application.ILogEventRepository;

/**
 * Provides methods for querying log events.
 */
@RestController
public class LogEventController extends BaseController {

	private static final Log logger = LogFactory.getLog(LogEventController.class);

	@Autowired
	private ILogEventRepository logEventRepository;

	/**
	 * Returns application events.
	 * 
	 * @param request
	 *            the request
	 * @return the events
	 */
	@RequestMapping(value = "/action/admin/logging/events", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse getEvents(@RequestBody LogEventQueryRequest request) {
		try {
			// Set default values
			if (request.getQuery() == null) {
				request.setQuery(new LogEventQuery());
			}
			if ((request.getQuery().getIndex() == null) || (request.getQuery().getIndex() < 0)) {
				request.getQuery().setIndex(0);
			}
			if (request.getQuery().getSize() == null) {
				request.getQuery().setSize(10);
			}

			LogEventQueryResult result = logEventRepository.getLogEvents(request.getQuery());

			LogEventQueryResponse response = new LogEventQueryResponse();

			response.setTotal(result.getTotal());

			response.setIndex(request.getQuery().getIndex());
			response.setSize(request.getQuery().getSize());

			List<LogEvent> events = new ArrayList<LogEvent>();

			for (LogEventEntity entity : result.getEvents()) {
				LogEvent e = new LogEvent();

				e.setAccount(entity.getAccount());
				e.setCategory(entity.getCategory());
				e.setCode(entity.getCode());
				e.setId(entity.getId());
				e.setLevel(entity.getLevel());
				e.setLogger(entity.getLogger());
				e.setMessage(entity.getMessage());
				e.setRemoteAddress(entity.getRemoteAddress());
				e.setTimestamp(entity.getTimestamp().getMillis());

				events.add(e);
			}
			response.setEvents(events);

			return response;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			RestResponse errorResponse = new RestResponse();
			errorResponse.add(this.getError(ex));

			return errorResponse;
		}
	}

}
