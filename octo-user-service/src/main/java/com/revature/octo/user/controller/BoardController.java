package com.revature.octo.user.controller;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.revature.octo.user.model.BoardUserJoin;
import com.revature.octo.user.model.SystemUser;
import com.revature.octo.user.repository.BoardUserJoinRepository;
import com.revature.octo.user.repository.SystemUserRepository;

@RestController
public class BoardController {
	
	@Autowired
	SystemUserRepository userRepo;
	
	@Autowired
	BoardUserJoinRepository boardUserRepo;

	@GetMapping(path="/getBoardsForUser/{userId}")
	public ResponseEntity<String> getBoardIds(@PathVariable String userId, HttpServletRequest request) {
		int userNum = Integer.parseInt(userId);
		SystemUser systemUser = userRepo.findOne(userNum);
		Set<BoardUserJoin> boardSet = systemUser.getBoardUserJoins();
		String boardList = "";
		for (BoardUserJoin buj : boardSet) {
			if (boardList.length() > 0) {
				boardList += ",";
			}
			boardList += buj.getBoardId();
		}
		return new ResponseEntity<String>(boardList, HttpStatus.OK);
	}

	@GetMapping(path="/getAllUserNotOnBoard/{boardId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SystemUser>> getAllUsersOnBoard(@PathVariable String boardId, HttpServletRequest request) {
		int boardNum = Integer.parseInt(boardId);
		System.out.println("boardId: " + boardId);
		
		List<SystemUser> systemUserListOnBoard = userRepo.findByBoardUserJoins_boardId(boardNum);
		System.out.println("users on baord: " + systemUserListOnBoard.size());
		
		List<SystemUser> systemUserList = (List<SystemUser>) userRepo.findAll();
		
		System.out.println("users NOT on board: " + systemUserListOnBoard.size());
		
		for (SystemUser su : systemUserListOnBoard) {
			systemUserList.remove(su);
		}
		System.out.println("Final: " + systemUserList);
		return new ResponseEntity<List<SystemUser>>(systemUserList, HttpStatus.OK);
		
	}
	
	@GetMapping(path="/getUsersOnBoard/{boardId}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SystemUser>> getUsersOnBoard(@PathVariable String boardId){
		int boardNum = Integer.parseInt(boardId);
		List<SystemUser> usersOnBoard = (List<SystemUser>)userRepo.findByBoardUserJoins_boardId(boardNum);
		return new ResponseEntity<List<SystemUser>>(usersOnBoard, HttpStatus.OK);
	}
	
	@PostMapping(path="/updateBoardUsers/{boardId}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<SystemUser>> updateBoardUsers(@PathVariable String boardId, @RequestBody List<SystemUser> updatedList){
		Integer boardNum = Integer.parseInt(boardId);
		List<SystemUser> currentBoardUsers = (List<SystemUser>) userRepo.findByBoardUserJoins_boardId(boardNum);
		
		System.out.println("CURRENT BOARD USERS: " + currentBoardUsers);
		System.out.println("NEW LIST OF USERS:" + updatedList);
		
		SystemUser su = null;
		SystemUser cbu = null;
		int size = (updatedList.size() > currentBoardUsers.size()) ? updatedList.size() : currentBoardUsers.size();
		for(int i=0; i < size; i++) {
			if(updatedList.get(i) != null) {
				su = updatedList.get(i);
				if(!currentBoardUsers.contains(su)){
					BoardUserJoin newBuj = new BoardUserJoin(boardNum, su);
					su.getBoardUserJoins().add(newBuj);
					boardUserRepo.save(newBuj);
					userRepo.save(su);
					currentBoardUsers.add(su);
				}
			}
			if(currentBoardUsers.get(i) != null) {
				cbu = currentBoardUsers.get(i);
				if(!updatedList.contains(cbu)) {
					BoardUserJoin oldBuj = new BoardUserJoin(boardNum, cbu);
					cbu.getBoardUserJoins().remove(oldBuj);
					boardUserRepo.delete(oldBuj);
					userRepo.save(cbu);
					currentBoardUsers.remove(cbu);
				}
			}	
		}
		System.out.println("current board users: " + currentBoardUsers);
		//currentBoardUsers now contains new members and old members have been removed, existing users remain
		return new ResponseEntity<List<SystemUser>>(currentBoardUsers, HttpStatus.OK);
	}
}
