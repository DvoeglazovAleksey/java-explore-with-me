package ru.practicum.error.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.controllers.admin.AdminCategoryController;
import ru.practicum.controllers.admin.AdminCompilationController;
import ru.practicum.controllers.admin.AdminEventController;
import ru.practicum.controllers.admin.AdminUserController;
import ru.practicum.controllers.priv.PrivateEventController;
import ru.practicum.controllers.priv.PrivateRequestController;
import ru.practicum.controllers.pub.PublicCategoryController;
import ru.practicum.controllers.pub.PublicCompilationController;
import ru.practicum.controllers.pub.PublicEventController;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.model.ErrorResponse;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.NotFoundException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice(assignableTypes = {
        AdminUserController.class,
        AdminCategoryController.class,
        PublicCategoryController.class,
        PrivateRequestController.class,
        AdminEventController.class,
        PublicEventController.class,
        PrivateEventController.class,
        AdminCompilationController.class,
        PublicCompilationController.class
})
@Slf4j
public class ErrorHandlerStats {
    @ExceptionHandler({ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            MissingPathVariableException.class,
            BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(Exception e) throws Exception {
        if (e instanceof ConstraintViolationException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MissingPathVariableException ||
                e instanceof BadRequestException) {
            return new ErrorResponse("Validation error: ", e.getMessage());
        }
        throw e;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Not found error: ", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final ConflictException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse("Conflict error: ", e.getMessage());
    }
}
