package it.course.myblogc3.controller;

import java.time.Instant;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.course.myblogc3.entity.Post;
import it.course.myblogc3.entity.User;
import it.course.myblogc3.entity.Voting;
import it.course.myblogc3.entity.VotingId;
import it.course.myblogc3.payload.response.ApiResponseCustom;
import it.course.myblogc3.repository.PostRepository;
import it.course.myblogc3.repository.VotingRepository;
import it.course.myblogc3.service.UserService;

@RestController
@Validated
public class VotingController {

	@Autowired
	PostRepository postRepository;
	@Autowired
	UserService userService;
	@Autowired
	VotingRepository votingRepository;

	
	@PostMapping("private/create-vote")
	@PreAuthorize("hasRole('READER')")
	public ResponseEntity<ApiResponseCustom> votePost(@RequestParam Long postId, @RequestParam @Min(1) @Max(5) int vote,HttpServletRequest request){

		Long pid = postRepository.getPostId(postId);
		if(pid==null)
			return new ResponseEntity<ApiResponseCustom>(
					new ApiResponseCustom(Instant.now(), 404, "OK", "Post not found", request.getRequestURI()), HttpStatus.NOT_FOUND);

		User u = userService.getAuthenticationUser();
				
		Optional<Voting> sv = votingRepository.findById(new VotingId(new Post(pid), new User(u.getId())));
		
		String msg;
		if(sv.isPresent()) {
			votingRepository.updateVote(vote, u.getId(), postId);
			msg="updated";
		}
		else {
			votingRepository.insertVote(vote, u.getId(), postId);
			msg="added";
		}
		
		//Voting v = new Voting(new VotingId(new Post(pid), new User(u.getId())), vote);		
		//votingRepository.save(v);
		
		return new ResponseEntity<ApiResponseCustom>(new ApiResponseCustom(
				Instant.now(),201,"OK", "New voting has been "+msg ,request.getRequestURI() ),HttpStatus.OK);
		
		}
	
	
}
