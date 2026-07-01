package com.app.nouapp.controller;

import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.nouapp.dto.StudyMaterialDto;
import com.app.nouapp.model.AdminInfo;
import com.app.nouapp.model.Enquiry;
import com.app.nouapp.model.Response;
import com.app.nouapp.model.StudentInfo;
import com.app.nouapp.model.StudyMaterial;
import com.app.nouapp.service.AdminInfoRepository;
import com.app.nouapp.service.EnquiryRepository;
import com.app.nouapp.service.ResponseRepository;
import com.app.nouapp.service.StudentInfoRepository;
import com.app.nouapp.service.StudyMaterialRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	AdminInfoRepository adrepo;

	@Autowired
	StudentInfoRepository stdrepo;
	
	@Autowired
	EnquiryRepository eqrepo;

	@Autowired
	ResponseRepository resrepo;
	
	@Autowired
	StudyMaterialRepository smrepo;

	@GetMapping("/adhome")
	public String ShowAdminDashboard(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());
			
			int scount = (int)stdrepo.count();
			model.addAttribute("scount", scount);
			
			List<StudyMaterial> smlist = smrepo.findByMaterialType("studymaterial");
			int smcount = smlist.size();
			model.addAttribute("smcount", smcount);
			
			List<StudyMaterial> aslist = smrepo.findByMaterialType("assignment");
			int ascount = aslist.size();
			model.addAttribute("ascount", ascount);
			List<Response> flist=resrepo.findByResponseType("feedback");
			int fcount=flist.size();
			model.addAttribute("fcount", fcount);
			List<Response> clist=resrepo.findByResponseType("complaint");
			int ccount=clist.size();
			model.addAttribute("ccount", ccount);
			
			int ecount=(int)eqrepo.count();
			model.addAttribute("ecount", ecount);
			
			return "admin/admindashboard";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/managestudent")
	public String ShowManageStudents(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			List<StudentInfo> slist = stdrepo.findAll();
			model.addAttribute("slist", slist);
			// model.addAttribute("count", slist.size());
			return "admin/managestudent";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired!");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/deletestudent")
	public String DeleteStudent(@RequestParam long enroll, HttpSession session, RedirectAttributes attributes,
			Model model) {
		if (session.getAttribute("admin") != null) {
			StudentInfo stdinfo = stdrepo.findById(enroll).get();
			stdrepo.delete(stdinfo);
			attributes.addFlashAttribute("msg", stdinfo.getName() + " is deleted Successfully");
			return "redirect:/admin/managestudent";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/addstudymaterial")
	public String ShowAddStudyMaterial(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());
			
			StudyMaterialDto dto = new StudyMaterialDto();
			model.addAttribute("dto", dto);
			
			return "admin/addstudymaterial";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	@PostMapping("/addstudymaterial")
	public String AddStudyMaterial(@ModelAttribute StudyMaterialDto dto, RedirectAttributes attributes)
	{
		try {
			
			MultipartFile filedata = dto.getFilename();
			String storageFileName = filedata.getOriginalFilename();
			long size = filedata.getSize();
			int s =(int) size/(1024*1024);	//file size in MB
			if(s>5)
			{
				attributes.addFlashAttribute("msg", "File Should be less that 5 MB");
				return "redirect:/admin/addstudymaterial";
			}
			System.err.println(size);
			String uploadDir = "public/mat/";
			Path UploadPath = Paths.get(uploadDir);
			
			if(!Files.exists(UploadPath))
			{
				Files.createDirectories(UploadPath);
			}
			
			try(InputStream inputStream = filedata.getInputStream()){
				Files.copy(inputStream, Paths.get(uploadDir +storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
			
			StudyMaterial material = new StudyMaterial();
			material.setCourse(dto.getCourse());
			material.setBranch(dto.getBranch());
			material.setYear(dto.getYear());
			material.setSubject(dto.getSubject());
			material.setTopic(dto.getTopic());
			material.setMaterialtype(dto.getMaterialtype());
			material.setFilename(storageFileName);
			
			Date dt = new Date();
			SimpleDateFormat df = new SimpleDateFormat();
			String posteddate = df.format(dt);
			material.setPosteddate(posteddate);
			smrepo.save(material);
			attributes.addFlashAttribute("msg", "Material Uploaded Successfully");
			return "redirect:/admin/addstudymaterial";
			
		} catch (Exception e) {
			attributes.addFlashAttribute("msg", "Something went wrong "+e.getMessage());
			return "redirect:/admin/addstudymaterial";
		}
	}
	
	@GetMapping("/viewenquiry")
	public String ShowViewEnquiry(HttpSession session, RedirectAttributes attributes, Model model) {
		if(session.getAttribute("admin")!=null) {
			AdminInfo ad=adrepo.getById(session.getAttribute("admin").toString());
			
			model.addAttribute("name", ad.getName());
			List<Enquiry>eList = eqrepo.findAll();
			model.addAttribute("elist",eList);
			return "admin/viewenquiry";
		}
		else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	@GetMapping("/deleteenquiry")
	public String DeleteEnquiry(@RequestParam int id, HttpSession session, RedirectAttributes attributes, Model model)
	{
		if(session.getAttribute("admin")!=null) {
			Enquiry eq = eqrepo.findById(id).get();
			eqrepo.delete(eq);
			attributes.addFlashAttribute("msg", eq.getName() + "is Deleted Successfully");
			return "redirect:/admin/managestudent";
		}else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	
	@GetMapping("/managestudymaterial")
	public String ShowManageStudyMaterial(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			List<StudyMaterial> smlist =smrepo.findAll();
			model.addAttribute("smlist", smlist);
			
			model.addAttribute("name", ad.getName());
			
			
			return "admin/managestudymaterial";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/deletemanagestudymaterial")
	public String DeleteStudyMaterial (@RequestParam int id, HttpSession session, RedirectAttributes attributes, Model model)
	{
		if(session.getAttribute("admin")!=null) {
			StudyMaterial sm=smrepo.findById(id).get();
			smrepo.delete(sm);
			attributes.addFlashAttribute("msg", sm.getFilename() + "is deleted Successfully");
			return "redirect:/admin/addstudymaterial";
		}else {
			attributes.addFlashAttribute("msg", "session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	@GetMapping("/viewfeedback")
	public String ShowFeedback(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			List<Response> flist = resrepo.findByResponseType("feedback");
			model.addAttribute("flist", flist);

			return "admin/viewfeedback";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}
	
	@GetMapping("/deletefeedback")
	public String DeleteFeedback(@RequestParam int id, HttpSession session, RedirectAttributes attributes, Model model) {
		if(session.getAttribute("admin")!=null) {
			Response res =resrepo.findById(id).get();
			resrepo.delete(res);
			attributes.addFlashAttribute("msg", res.getName() + "is deleted successfully");
			return "redirect:/admin/viewfeedback";
		}
		else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	
	@GetMapping("/viewcomplaint")
	public String ShowComplaint(HttpSession session, RedirectAttributes attributes, Model model) {
		if (session.getAttribute("admin") != null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());

			List<Response> clist = resrepo.findByResponseType("complaint");
			model.addAttribute("clist", clist);
			return "admin/viewcomplaint";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@GetMapping("/deletecomplaint")
	public String DeleteComplaint(@RequestParam int id, HttpSession session , RedirectAttributes attributes, Model model) {
		if(session.getAttribute("admin")!=null) {
			Response res = resrepo.findById(id).get();
			resrepo.delete(res);
			attributes.addFlashAttribute("msg", res.getName() + "is deleted successfully");
			return "redirect:/admin/viewcomplaint";
		}
		else
		{
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}

		}
	
	@GetMapping("/changepassword")
	public String ShowChangePassword(HttpSession session, Model model, RedirectAttributes attributes) {
		if (session.getAttribute("admin")!= null) {
			AdminInfo ad = adrepo.getById(session.getAttribute("admin").toString());
			model.addAttribute("name", ad.getName());
			return "admin/changepassword";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired");
			return "redirect:/adminlogin";
		}
	}

	@PostMapping("/changepassword")
	public String ChangePassword(HttpSession session, RedirectAttributes attributes, HttpServletRequest request) {
		try {

			AdminInfo adinfo = adrepo.findById(session.getAttribute("admin").toString()).get();
			String oldpass = request.getParameter("oldpass");
			String newpass = request.getParameter("newpass");
			String confirmpass = request.getParameter("confirmpass");

			if (newpass.equals(confirmpass)) 
			{
				if (oldpass.equals(adinfo.getPassword())) 
				{
					adinfo.setPassword(confirmpass);
					adrepo.save(adinfo);
					session.invalidate();
					attributes.addFlashAttribute("msg", "Password Changed Succesfully 😀");
					return "redirect:/adminlogin";
				}
				else {
					attributes.addFlashAttribute("message", "Invalid Old Password");
				}
			} 
			else {
				attributes.addFlashAttribute("message", "New Password & Confirm Password Not Matched!");
			}
			return "redirect:/admin/changepassword";

		} catch (Exception e) {
			attributes.addFlashAttribute("message", "Something went wrong " + e.getMessage());
			return "redirect:/admin/changepassword";
		}
	}

	@GetMapping("/logout")
	public String Logout(HttpSession session, RedirectAttributes attributes) {
		if (session.getAttribute("admin") != null) {
			session.invalidate();
			attributes.addFlashAttribute("msg", "Successfully Logout");
			return "redirect:/adminlogin";
		} else {
			attributes.addFlashAttribute("msg", "Session Expired!");
			return "redirect:/adminlogin";
		}
	}
}
