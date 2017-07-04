package fluvial;

import fluvial.model.job.JobFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by superttmm on 01/06/2017.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackageClasses = {FluvialConfig.class})
public class FluvialConfig {

    @Bean
    @Scope("singleton")
    public JobFactory jobFactory(){
        JobFactory jobFactory = new JobFactory();
        jobFactory.initJobMetadataMap();
        return jobFactory;
    }
}
