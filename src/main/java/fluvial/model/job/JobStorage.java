package fluvial.model.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fluvial.model.performer.PerformerStorage;
import fluvial.model.storage.Store;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by superttmm on 22/05/2017.
 */
@Entity
public abstract class JobStorage implements Store {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus = JobStatus.Requested;

    private String jobType;

    @Column(name = "specificJob", columnDefinition = "json")
    @Convert(converter = SpecificJobJsonConverter.class)
    private Job specificJob;

    private Date createTime;

    private Date startTime;

    private Date updateTime;

    @OneToMany(fetch = FetchType.EAGER,cascade = {CascadeType.REMOVE}, mappedBy = "parentJob")
    private List<JobStorage> subJobs;

    @JsonIgnore
    @ManyToOne
    @JoinColumn
    private JobStorage parentJob;

    @ManyToOne
    @JoinColumn
    private PerformerStorage performer;

    @Version
    private Long version;

    public Long getId(){return id;}

    public String getJobType() { return jobType; }

    public void setJobType(String jobType) {this.jobType = jobType;}

    public JobStatus getJobStatus(){return jobStatus;}

    public void setJobStatus(JobStatus status) {this.jobStatus = status;}

    public Job getSpecificJob() {
        if(specificJob != null){
            specificJob.jobStorage = this;
        }
        return specificJob;
    }

    public void setSpecificJob(Job job) { this.specificJob = job; }

    public Date getStartTime(){return startTime;}

    public void setStartTime(Date date) {this.startTime = date;}

    public List<JobStorage> getSubJobs() {return subJobs;}

    public void setSubJobs(List<JobStorage> subJobs) {this.subJobs = subJobs;}

    public void addSubJob(JobStorage subJob) {
        if(subJobs == null){
            subJobs = new ArrayList<>();
        }
        this.subJobs.add(subJob);
    }

    public JobStorage getParentJob() {return parentJob;}

    public void setParentJob(JobStorage parentJob) {this.parentJob = parentJob;}

    public PerformerStorage getPerformer() {return performer;}

    public void setPerformer(PerformerStorage performer) {this.performer = performer;}

    public Date getCreateTime() {return createTime;}

    public void setCreateTime(Date createTime) {this.createTime = createTime;}

    public Date getUpdateTime() {return updateTime;}

    public void setUpdateTime(Date updateTime) {this.updateTime = updateTime;}
}
