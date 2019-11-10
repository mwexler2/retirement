package name.wexler.retirement.visualizer.CashFlowEstimator;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import name.wexler.retirement.visualizer.Context;
import name.wexler.retirement.visualizer.Job;
import name.wexler.retirement.visualizer.Security;

import java.util.Collections;

@JsonIgnoreProperties(ignoreUnknown = true)
abstract public class EquityCompensation extends CashFlowEstimator {
    @JsonIgnore
    private Job job;
    private Security security;
    private final int totalShares;

    public EquityCompensation(Context context,
               String id,
               String jobId,
               String cashFlowId,
               String securityId,
               int totalShares) throws DuplicateEntityException {
        super(context, id, cashFlowId,
                Collections.singletonList(((Job) context.getById(Job.class, jobId)).getEmployee()),
                Collections.singletonList(((Job) context.getById(Job.class, jobId)).getEmployer()));
        this.setJobId(context, jobId);
        this.setSecurityId(context, securityId);
        this.totalShares = totalShares;
    }

    @JsonIgnore
    Job getJob() {
        return job;
    }

    abstract public String getName();

    @JsonProperty(value = "job")
    public String getJobId() {
        return job.getId();
    }

    public int getTotalShares() {
        return this.totalShares;
    }

    private void setJobId(@JacksonInject("context") Context context, @JsonProperty(value = "job", required = true) String jobId) {
        this.job = context.getById(Job.class, jobId);
    }

    private void setSecurityId(@JacksonInject("context") Context context, @JsonProperty(value = "security", required = true) String securityId) {
        this.security = context.getById(Security.class, securityId);
    }

    @JsonProperty("security")
    public String getSecurityName() {
        return this.security.getName();
    }

    @JsonIgnore
    Security getSecurity() {
        return security;
    }


}
