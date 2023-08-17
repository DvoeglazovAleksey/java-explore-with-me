package ru.practicum.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.exception.BadRequestException;

import java.time.LocalDateTime;

@UtilityClass
public class ExploreConstantsAndStaticMethods {

        /*----------Category String Utils------------*/
        public static final String CATEGORY_NAME_ALREADY_EXISTS_EXCEPTION = "Category name already exists.";
        public static final String CATEGORY_NOT_FOUND_EXCEPTION = "Category not found.";
        public static final String CATEGORY_IS_CONNECTED_WITH_EVENTS =
                "Category is connected with events and could be deleted.";

        /*----------User String Utils------------*/
        public static final String USER_NOT_FOUND_EXCEPTION_MESSAGE = "User not found or unavailable.";
        public static final String USER_NAME_ALREADY_EXISTS = "User name already exists and could be saved.";

        /*----------Event String Utils------------*/
        public static final String EVENT_NOT_FOUND_EXCEPTION = "Event not found.";
        public static final String INVALID_EVENT_DATE = "Invalid event date-time.";
        public static final String INVALID_EVENT_STATUS = "Invalid event status.";
        public static final String EVENT_PARTICIPANTS_LIMIT_IS_REACHED = "Participants limit is reached.";
        public static final String EVENT_SEARCH_INVALID_PARAMETERS = "Invalid search parameters.";
        public static final String EVENT_INCORRECT_TIME_RANGE_FILTER = "Invalid time-range filter params.";

        /*----------Compilation String Utils------------*/
        public static final String COMPILATION_NOT_FOUND = "Compilation not found.";
        public static final String COMPILATION_TITLE_ALREADY_EXIST =
                "Compilation title already exists and could not be used";

        /*----------Request String Utils------------*/
        public static final String REQUEST_ALREADY_EXIST = "Participation request already exists.";
        public static final String EVENT_REQUEST_STATUS_CHANGE_FORBIDDEN = "Impossible to change request status.";
        public static final String PARTICIPATION_REQUEST_NOT_FOUND = "Participation request not found.";
        public static final String OWNER_NOT_ALLOWED_TO_ADD_REQUEST =
                "Event owner not allowed to create request to his own event.";

        public static Pageable pageRequestOf(int from, int size) {
                int page = from / size;
                return PageRequest.of(page, size);
        }

        public static void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
                LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
                if (value.isBefore(minValidDateTime)) {
                        throw new BadRequestException(INVALID_EVENT_DATE);
                }
        }


}