package info.developerblog.spring.cloud.marathon;

import mesosphere.marathon.client.MarathonClient;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by aleksandr on 07.07.16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(ConditionalOnMarathonEnabled.OnMarathonEnabledCondition.class)
public @interface ConditionalOnMarathonEnabled {

    class OnMarathonEnabledCondition extends AllNestedConditions {

        public OnMarathonEnabledCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(value = "spring.cloud.marathon.enabled", matchIfMissing = true)
        static class FoundProperty {}

        @ConditionalOnClass(MarathonClient.class)
        static class FoundClass {}
    }
}