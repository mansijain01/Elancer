package com.me.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.me.dao.ApplicantDAOImpl;
import com.me.dao.ApplicationDAOImpl;
import com.me.dao.ClientDAOImpl;
import com.me.dao.JobCategoryDAOImpl;
import com.me.dao.JobDAOImpl;
import com.me.dao.PersonDAOImpl;
import com.me.exception.AdException;
import com.me.pojo.Applicant;
import com.me.pojo.Client;
import com.me.pojo.Job;
import com.me.pojo.JobApplication;
import com.me.pojo.JobCategory;
import com.me.pojo.Person;
import com.me.springview.PdfReportView;

@Controller
@SessionAttributes("username")
public class JobController {

	private static final int JobApplication = 0;

	@Autowired
	@Qualifier("personDao")
	PersonDAOImpl personDao;

	@Autowired
	@Qualifier("applicantDao")
	ApplicantDAOImpl applicantDao;

	@Autowired
	@Qualifier("clientDao")
	ClientDAOImpl clientDao;

	@Autowired
	@Qualifier("jobDao")
	JobDAOImpl jobDao;

	@Autowired
	@Qualifier("applicationDao")
	ApplicationDAOImpl applicationDao;

	@Autowired
	@Qualifier("categoryDao")
	JobCategoryDAOImpl categoryDao;

	@Autowired
	ServletContext servletContext;

	@RequestMapping(value = "/addJobClient.htm", method = RequestMethod.GET)
	protected String addJobPage(@ModelAttribute("job") Job job, BindingResult result, HttpServletRequest request,
			Model model) throws Exception {
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		ArrayList<JobCategory> categoryList = new ArrayList<JobCategory>();
		categoryList = categoryDao.findAll();

		ArrayList<String> categoryName = new ArrayList<String>();

		for (JobCategory jc : categoryList) {
			categoryName.add(jc.getCategoryName());
		}

		// System.out.println(categoryList.get(0).getCategoryName());

		model.addAttribute("list", categoryList);
		model.addAttribute("namelist", categoryName);
		return "postJob";
	}

	@RequestMapping(value = "/addJob.htm", method = RequestMethod.POST)
	protected String addJobForm(@ModelAttribute("job") Job job, BindingResult result, HttpServletRequest request,
			ModelMap model) throws Exception {
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		try {

			// System.out.println(job.getJobCategory().getCategoryId());
			String username = (String) session.getAttribute("username");

			System.out.println(username);

			Person person = (Person) personDao.findByUsername(username);
			// System.out.println(person.getFirstName());
			long id = person.getPersonID();
			Client client = (Client) clientDao.findById(id);
			job.setPostedBy(client);

			System.out.println("JOB " + job.getPostedBy().getFirstName());
			System.out.println(id);
			System.out.println("IDDDDDD" + job.getPostedBy().getPersonID());

			int catid = Integer.parseInt(request.getParameter("catId"));
			JobCategory selectedCategory = new JobCategory();
			selectedCategory = categoryDao.findById(catid);

			job.setJobCategory(selectedCategory);

			jobDao.save(job);
			System.out.println("Saved new job");
			return "jobAdded";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error";
	}

	@RequestMapping(value = "/viewMyJobs.htm", method = RequestMethod.GET)
	protected String viewCategory(@ModelAttribute("jobCategory") JobCategory jobCategory, BindingResult result,
			HttpServletRequest request, Model model) throws Exception {

		// System.out.println("11111111111111111111");
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }

		String username = (String) session.getAttribute("username");

		ArrayList<Job> jobList = new ArrayList<Job>();
		jobList = jobDao.findByUserId(personDao.findByUsername(username).getPersonID());
		model.addAttribute("myJobList", jobList);
		return "showJobs";
	}

	@RequestMapping(value = "/searchJobs.htm", method = RequestMethod.POST)
	protected String signinForm(@ModelAttribute("job") Job job, BindingResult result, HttpServletRequest request,
			ModelMap model) throws Exception {
		ArrayList<JobCategory> categoryList = new ArrayList<JobCategory>();
		categoryList = categoryDao.findAll();

		ArrayList<String> categoryName = new ArrayList<String>();

		for (JobCategory jc : categoryList) {
			categoryName.add(jc.getCategoryName());
		}

		// System.out.println(categoryList.get(0).getCategoryName());

		model.addAttribute("list", categoryList);
		model.addAttribute("namelist", categoryName);
		return "searchJobs";
	}

	@RequestMapping(value = "/applyNow.htm", method = RequestMethod.GET)
	protected String jobApplication(@ModelAttribute("jobApplication") JobApplication jobApplication,
			BindingResult result, HttpServletRequest request, ModelMap model) {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		
		int i = Integer.parseInt(request.getParameter("id"));
		session.setAttribute("jobid", i);
		// System.out.println(i);
		return "applicationForm";
	}

	@RequestMapping(value = "/jobStatus.htm", method = RequestMethod.GET)
	protected String jobStatus(@ModelAttribute("jobApplication") JobApplication jobApplication, BindingResult result,
			HttpServletRequest request, Model model) throws AdException {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		
		int i = Integer.parseInt(request.getParameter("id"));
		Job job = jobDao.findById(i);
		String title = job.getJobTitle();
		ArrayList<JobApplication> applicationList = applicationDao.findById(i);
		model.addAttribute("applications", applicationList);
		model.addAttribute("title", title);
		// System.out.println(i);
		return "jobStatus";
	}

	@RequestMapping(value = "/application.htm", method = RequestMethod.POST)
	protected String applicationProcessing(@ModelAttribute("jobApplication") JobApplication jobApplication,
			BindingResult result, @RequestParam("resume") MultipartFile resume, HttpServletRequest request,
			ModelMap model) throws Exception {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		try {
			String username = (String) session.getAttribute("username");

			Applicant applicant = applicantDao.findByUsername(username);
			System.out.println("Applicant" + applicant.getFirstName());
			jobApplication.setAppliedBy(applicant);

			int id = (Integer) session.getAttribute("jobid");
			Job job = jobDao.findById(id);
			session.removeAttribute("jobid");

			File file;
			String check = File.separator; // For windows and Linux.
			String path = null;
			if (check.equalsIgnoreCase("\\")) {

				path = servletContext.getRealPath("").replace("build\\", "");
			}

			if (check.equalsIgnoreCase("/")) {

				path = servletContext.getRealPath("").replace("build/", "");

				path += "/resources/images/"; // For MAC.

			}
			path += "\\resources\\images\\";
			if (jobApplication.getResume() != null) {
				String fileNameWithExt = System.currentTimeMillis() + resume.getOriginalFilename();

				file = new File(path + fileNameWithExt);

				String context = servletContext.getContextPath();

				resume.transferTo(file);

				String photoName = context + "/resources/images/" + fileNameWithExt;

				jobApplication.setFileName(photoName);
			}

			// if (!resume.isEmpty()) {
			// try {
			// String uploadsDir = "/uploads/";
			// String realPathtoUploads =
			// servletContext.getRealPath(uploadsDir);
			// System.out.println(realPathtoUploads);
			// if(! new File(realPathtoUploads).exists())
			// {
			// new File(realPathtoUploads).mkdir();
			// }
			//
			// String orgName = resume.getOriginalFilename();
			// String filePath = realPathtoUploads + orgName;
			// File dest = new File(filePath);
			// resume.transferTo(dest);
			//
			// jobApplication.setFileName(filePath);

			jobApplication.setJob(job);

			jobApplication.setStatus("Applied");
			applicationDao.save(jobApplication);
			return "appliedSuccessfully";
		} catch (Exception e) {
			System.out.println("Couldnt save application");
		}
		return "errors";
	}

	@RequestMapping("/search.htm")
	public String searchPost(@ModelAttribute("job") Job job, BindingResult result, HttpServletRequest request,
			Model model) throws AdException {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		
		String keyword = request.getParameter("keyword");
		int categoryId = Integer.parseInt(request.getParameter("catId"));

		ArrayList<Job> searchList = (ArrayList<Job>) jobDao.findByKeyword(keyword, categoryId);
		model.addAttribute("searchList", searchList);
		return "searchResult";
	}

	@RequestMapping(value = "/details.htm", method = RequestMethod.GET)
	protected String moreDetails(@ModelAttribute("jobApplication") JobApplication jobApplication, BindingResult result,
			HttpServletRequest request, Model model) throws AdException {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		int id = Integer.parseInt(request.getParameter("id"));
		JobApplication ja = applicationDao.findByApplicationId(id);
		model.addAttribute("application", ja);
		return "applicantDetails";
	}

	@RequestMapping(value = "/result.htm", method = RequestMethod.GET)
	protected String applicationResult(@ModelAttribute("jobApplication") JobApplication jobApplication,
			BindingResult result, HttpServletRequest request, Model model) throws AdException {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		String res = request.getParameter("val");
		int id = Integer.parseInt(request.getParameter("app"));

		JobApplication application = applicationDao.findByApplicationId(id);

		if (res.equals("approved")) {
			applicationDao.updateStatus(id, "Approved");
		} else if (res.equals("rejected")) {
			applicationDao.updateStatus(id, "Rejected");
		} else {
			applicationDao.updateStatus(id, "Pending");
		}
		return "redirect:/viewMyJobs.htm";
	}

	@RequestMapping(value = "/viewAppliedJobs.htm", method = RequestMethod.GET)
	protected String appliedJobs(@ModelAttribute("jobApplication") JobApplication jobApplication, BindingResult result,
			HttpServletRequest request, @ModelAttribute("model") ModelMap model) throws AdException {
		
		HttpSession session = request.getSession();
        if(session.getAttribute("username")==null){
            return "redirect:/signin.htm";
        }
		String username = (String) session.getAttribute("username");

		Person person = personDao.findByUsername(username);
		long personId = person.getPersonID();

		ArrayList<JobApplication> applications = applicationDao.findByApplicantId(personId);
		model.addAttribute("applications", applications);
		return "showAppliedJobs";
	}

}
