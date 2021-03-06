package com.ssafy.pit.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ssafy.pit.common.auth.PitUserDetails;
import com.ssafy.pit.common.response.BaseResponseBody;
import com.ssafy.pit.entity.User;
import com.ssafy.pit.request.ClassSearchGetReq;
import com.ssafy.pit.request.CreateClassPostReq;
import com.ssafy.pit.request.SetVideoUrlsPostReq;
import com.ssafy.pit.response.ClassDetailGetRes;
import com.ssafy.pit.response.ClassListGetRes;
import com.ssafy.pit.response.RegisterClassGetRes;
import com.ssafy.pit.service.ClassService;
import com.ssafy.pit.service.PtroomService;
import com.ssafy.pit.service.UserService;

@CrossOrigin(
        origins = {"https://i5a204.p.ssafy.io:5000", "https://i5a204.p.ssafy.io:8083", "https://localhost:8083"}, 
        allowCredentials = "true", 
        allowedHeaders = "*", 
        methods = {RequestMethod.GET,RequestMethod.POST,RequestMethod.DELETE,RequestMethod.PUT,RequestMethod.OPTIONS}
)
@RequestMapping("/v1/class")
@RestController
public class ClassController {
	
	@Autowired
	ClassService classService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	PtroomService ptroomService;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	// ????????? ????????? ???????????? (?????? ???????????? ????????????, 001??? ??????)
	@GetMapping("/{classNo}")
	public ResponseEntity<ClassDetailGetRes> getClassDetail(@PathVariable int classNo){
		ClassDetailGetRes classDetail = classService.getClassDetail(classNo, "001");
		return ResponseEntity.status(200).body(classDetail);
	}
	
	// ????????? ????????? ????????????
	@GetMapping()
	public ResponseEntity<List<ClassListGetRes>> getClassList(ClassSearchGetReq searchInfo) {
		List<ClassListGetRes> classList = null;
		classList = classService.getClassList("001");
		if(searchInfo.getSearchType() == null && searchInfo.getClassDay() == null && searchInfo.getClassLevel() == null
				&& searchInfo.getClassType() == null && searchInfo.getClassStartTime() == null 
				&& searchInfo.getClassEndTime() == null && searchInfo.getSearchKeyword() == null) {
			classList = classService.getClassList("001");
		}
		else {
			classList = classService.getClassList(searchInfo, "001");
		}
		return ResponseEntity.status(200).body(classList);
	}	
	
	// ???????????? ?????? ????????? ????????? ???????????? (authentication) permission(??????, ?????????, ??????)??? ?????? ????????? ???????????? ??????
	@GetMapping("/admin")
	public ResponseEntity<List<ClassListGetRes>> getAdminClassList(Authentication authentication, @RequestParam HashMap<String, String> permissionMap) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		if(userService.validateUserType(userEmail) == 1) {
			List<ClassListGetRes> classList = classService.getClassList(permissionMap.get("permission"));
			return ResponseEntity.status(200).body(classList);
		}
		else {			
			return ResponseEntity.status(403).body(null);
		}
	}
	
	// ????????? ????????? ?????? -> ??????, ?????????, ????????? ????????? ?????? ???????????? ??????
	@GetMapping("/admin/{classNo}")
	public ResponseEntity<ClassDetailGetRes> getAdminClassDetail(Authentication authentication, @PathVariable int classNo) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		if(userService.validateUserType(userEmail) == 1) {
			ClassDetailGetRes classDetail = classService.getClassDetail(classNo, "000");
			return ResponseEntity.status(200).body(classDetail);
		}
		else {			
			return ResponseEntity.status(403).body(null);
		}		
	}
	
	
	// ?????? ?????? ????????????
	@GetMapping("/likes")
	public ResponseEntity<List<ClassListGetRes>> getClassLikesList(Authentication authentication) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		int userNo = userDetails.getUser().getUserNo();
		if(userService.validateUserType(userEmail) == 3) {
			List<ClassListGetRes> classLikesList = classService.getClassLikesList(userNo);
			return ResponseEntity.status(200).body(classLikesList);
		}
		else {
			return ResponseEntity.status(403).body(null);
		}
	}
	
	// ?????? ??????
	@PostMapping("/likes/{classNo}")
	public ResponseEntity<BaseResponseBody> registerClassLikes(Authentication authentication, @PathVariable int classNo){
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		int userNo = userDetails.getUser().getUserNo();
		if(userService.validateUserType(userEmail) == 3) {
			if(classService.registerClassLikes(userNo, classNo) == 1) {
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????????????????? ?????????????????????."));
			}
			else if(classService.registerClassLikes(userNo, classNo) == 2) {
				return ResponseEntity.status(200).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
			}
			else if(classService.registerClassLikes(userNo, classNo) == 3){				
				return ResponseEntity.status(409).body(BaseResponseBody.of(409, "?????? ?????? ??????????????????."));
			}
			else {
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "??????????????? ????????? ???????????????."));
				
			}
		}
		else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
		}
	}
	
	// ?????? ??????
	@DeleteMapping("/likes/{classNo}")
	public ResponseEntity<BaseResponseBody> deleteClassLikes(Authentication authentication, @PathVariable int classNo){
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		int userNo = userDetails.getUser().getUserNo();
		if(userService.validateUserType(userEmail) == 3) {
			if(classService.deleteClassLikes(userNo, classNo) == 1) {
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????????????????? ?????????????????????."));
			}
			else {				
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "?????? ????????? ??????????????? ????????? ???????????????."));
			}
		}
		else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
		}
	}
	
	// ???????????? ?????????
	@GetMapping("/finishedclass")
	public ResponseEntity<List<ClassListGetRes>> getFinishedClassList(Authentication authentication) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		int userNo = userDetails.getUser().getUserNo();
		if(userService.validateUserType(userEmail) == 3) {
			List<ClassListGetRes> finishedClassList = classService.getFinishedClassList(userNo);
			return ResponseEntity.status(200).body(finishedClassList);
		}
		else if(userService.validateUserType(userEmail) == 2) {
			List<ClassListGetRes> finishedClassList = classService.getFinishedTeachClassList(userNo);
			return ResponseEntity.status(200).body(finishedClassList);
		}
		else {
			return ResponseEntity.status(403).body(null);
		}
	}
	
	// ????????? ?????????
	@GetMapping("/registerclass")
	public ResponseEntity<List<RegisterClassGetRes>> getRegisterClassList(Authentication authentication) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		int userNo = userDetails.getUser().getUserNo();
		if(userService.validateUserType(userEmail) == 3) {
			List<RegisterClassGetRes> registerClassList = classService.getRegisterClassList(userNo);
			return ResponseEntity.status(200).body(registerClassList);
		}
		else if(userService.validateUserType(userEmail) == 2) {
			List<RegisterClassGetRes> registerClassList = classService.getTeachClassList(userNo);
			return ResponseEntity.status(200).body(registerClassList);
		}
		else {
			return ResponseEntity.status(403).body(null);
		}
	}
	
	// ????????? ?????? (??????????????? ??????????????? ????????????)
	@PostMapping("/create")
	public ResponseEntity<BaseResponseBody> createClass(Authentication authentication, @ModelAttribute CreateClassPostReq createClassInfo, MultipartHttpServletRequest request) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		User user = userDetails.getUser();
		String userEmail = userDetails.getUsername();
		if(userService.validateUserType(userEmail) == 2) {
			try {
				// ???????????? ?????? ??????
				classService.createClass(createClassInfo, user);
				// class_photo ???????????? ?????? classNo ??? ?????????
				int classNo = classService.getLatestClassNo();
				
				// ????????? ????????? userName, classTitle, classDay??? encode ?????? ptroomSessionName ??????
				String ptroomSessionName = 
						passwordEncoder.encode(user.getUserName()+createClassInfo.getClassTitle()+createClassInfo.getClassDay());
				
				// ptroom url ??????
				ptroomService.createPtroom(ptroomSessionName, classNo);
				
				// ????????? ????????? ??????
				classService.createClassPhoto(request, classNo);
				// ?????????????????? ??????
				classService.createSubPhotos(request, classNo);
				
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????? ???????????? ???????????? ?????????????????????."));				
			}
			catch (Exception e) {
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "????????? ???????????? ????????? ????????? ?????????????????????."));
			}			
		}
		else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
		}
	}
	
	// ????????? ???????????? (???????????? ????????? ????????????)
	@PutMapping("/enrollment/{classNo}")
	public ResponseEntity<BaseResponseBody> enrollClass(Authentication authentication, @PathVariable int classNo) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		User user = userDetails.getUser();
		String userEmail = userDetails.getUsername();
		if(userService.validateUserType(userEmail) == 3) {
			try {
				if(classService.enrollClass(user, classNo) == 1) {
					return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????? ???????????? ???????????????????????????."));	
				}
				else if (classService.enrollClass(user, classNo) == 2) {					
					return ResponseEntity.status(409).body(BaseResponseBody.of(409, "????????? ?????? ?????? ??????????????????."));
				}
				else {
					return ResponseEntity.status(405).body(BaseResponseBody.of(405, "?????????????????? ????????? ?????????????????????."));
				}
			}
			catch (Exception e) {
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "????????? ???????????? ????????? ????????? ?????????????????????."));
			}
		}
		else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
		}
	}
	
	// ????????? permission ?????? (???????????? ??????)
	@PutMapping("/admin/{classNo}")
	public ResponseEntity<BaseResponseBody> updateClassPermission(Authentication authentication, @RequestBody HashMap<String, String> permissionMap, @PathVariable int classNo) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		User user = userDetails.getUser();
		String userEmail = userDetails.getUsername();
		if(userService.validateUserType(userEmail) == 1) {
			try {
				String permission = permissionMap.get("permission");
				classService.updateClassPermission(classNo, permission);
				if(permission.equals("001")) {
					return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????????????????? ???????????? ???????????????."));	
				} else if(permission.equals("002")) {
					return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????????????????? ??????????????? ???????????????."));	
				} else if(permission.equals("003")) {
					return ResponseEntity.status(200).body(BaseResponseBody.of(200, "?????????????????? ???????????? ???????????????."));	
				} else {
					return ResponseEntity.status(500).body(BaseResponseBody.of(500, "????????? ???????????? ????????? ????????? ?????????????????????."));
				}
			} catch (Exception e) {
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "????????? ???????????? ????????? ????????? ?????????????????????."));
			}
			
		}
		else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
		}
	}
	
	// ????????? URL DB??? ??????
	@PostMapping("/video/{classNo}")
	public ResponseEntity<BaseResponseBody> setVideoUrls(Authentication authentication, @PathVariable int classNo, @RequestBody SetVideoUrlsPostReq setVideoUrlsInfo){
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		User user = userDetails.getUser();
		String userEmail = userDetails.getUsername();
		int userNo = user.getUserNo();
		
		if(userService.validateUserType(userEmail) == 3) {
			try {
				classService.setVideoUrls(userNo, classNo, setVideoUrlsInfo);
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "DB??? URL??? ?????????????????????."));
			}
			catch (Exception e) {
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "DB ????????? ??????????????????."));
			}
		} else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ?????? ??????????????????."));
		}
	}
	
	// ?????? ???????????? ???????????? ?????????
	@GetMapping("/video/{classNo}")
	public ResponseEntity<Map<String, List>> getVideoUrls(Authentication authentication, @PathVariable int classNo) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		User user = userDetails.getUser();
		String userEmail = userDetails.getUsername();
		int userNo = user.getUserNo();
		if(userService.validateUserType(userEmail) == 3) {
			try {				
				List<String> videoUrls = classService.getVideoUrls(userNo, classNo);
				List<Date> videoSaveTimes = classService.getSaveTimtes(userNo, classNo);
				Map<String, List> map = new HashMap<String, List>();
				map.put("videoUrls", videoUrls);
				map.put("videoSaveTimes", videoSaveTimes);
				return ResponseEntity.status(200).body(map);
			}
			catch (Exception e) {
				return ResponseEntity.status(500).body(null);
			}
			
		} else {
			return ResponseEntity.status(403).body(null);
		}
	}
	
	// ????????? ???????????? ????????????
	@PutMapping("/cnt/{classNo}")
	public ResponseEntity<BaseResponseBody> updateClassCnt(Authentication authentication, @PathVariable int classNo) {
		PitUserDetails userDetails = (PitUserDetails) authentication.getDetails();
		String userEmail = userDetails.getUsername();
		if(userService.validateUserType(userEmail) == 2) {
			try {
				classService.addClassCnt(classNo);
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "????????? ??????????????? ?????????????????????."));
			}
			catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(500).body(BaseResponseBody.of(500, "????????? ????????? ???????????????."));
			}
		} else {
			return ResponseEntity.status(403).body(BaseResponseBody.of(403, "????????? ??? ?????? ??????????????????."));
		}
	}
	
}
