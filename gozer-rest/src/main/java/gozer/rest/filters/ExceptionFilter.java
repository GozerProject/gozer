package gozer.rest.filters;

import com.google.common.base.Optional;
import gozer.rest.exceptions.UnknownProjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.*;
import restx.factory.Component;

import java.io.IOException;

@Component
public class ExceptionFilter implements RestxFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionFilter.class);

    @Override
    public Optional<RestxRouteMatch> match(RestxRequest req) {
        return Optional.of(new RestxRouteMatch(this, "*", req.getRestxPath()));
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        RestxRouteMatch next = ctx.nextHandlerMatch();
        try {
            next.getHandler().handle(next, req, resp, ctx);
        } catch (UnknownProjectException e) {
            throw new WebException(HttpStatus.NOT_FOUND, "Project "+e.getProjectName()+"is unknown");
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
            throw new WebException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
