package cz.cuni.matfyz.server.utils;

import cz.cuni.matfyz.server.entity.Job;

/**
 *
 * @author jachymb.bartik
 */
public class RunJobData {

	public Job job;
    public UserStore store;

    public RunJobData(Job job, UserStore store) {
        this.job = job;
        this.store = store;
    }
    
}
