package com.me.dao;

import java.util.ArrayList;

import com.me.exception.AdException;
import com.me.pojo.JobCategory;

public interface JobCategoryDAO {
	public void save(JobCategory jobCategory) throws Exception;
	public ArrayList<JobCategory> findAll() throws Exception;
	public JobCategory findById(int categoryId) throws AdException;
}
