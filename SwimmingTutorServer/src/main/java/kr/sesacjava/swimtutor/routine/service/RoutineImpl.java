package kr.sesacjava.swimtutor.routine.service;

import jakarta.transaction.Transactional;
import kr.sesacjava.swimtutor.routine.dto.RequestRoutineDTO;
import kr.sesacjava.swimtutor.routine.dto.ResponseRoutineDTO;
import kr.sesacjava.swimtutor.routine.dto.RoutineDetailDTO;
import kr.sesacjava.swimtutor.routine.dto.TrainingForRoutineDTO;
import kr.sesacjava.swimtutor.routine.entity.Routine;
import kr.sesacjava.swimtutor.routine.entity.Training;
import kr.sesacjava.swimtutor.routine.entity.TrainingForRoutine;
import kr.sesacjava.swimtutor.routine.entity.id.RoutineId;
import kr.sesacjava.swimtutor.routine.repository.RoutineRepository;
import kr.sesacjava.swimtutor.routine.repository.RoutineSequenceUpdateRepository;
import kr.sesacjava.swimtutor.routine.repository.TrainingForRoutineRepository;
import kr.sesacjava.swimtutor.routine.repository.TrainingRepository;
import kr.sesacjava.swimtutor.security.CurrentUser;
import kr.sesacjava.swimtutor.security.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Log4j2
public class RoutineImpl implements RoutineService {

    private static final Logger LOG = LoggerFactory.getLogger(RoutineImpl.class);
    private RoutineRepository routineRepo;
    private TrainingRepository trainingRepo;
    private TrainingForRoutineRepository trainingForRoutineRepo;
    private RoutineSequenceUpdateRepository routineSequenceUpdateRepo;

    @Autowired
    public RoutineImpl(RoutineRepository routineRepo, TrainingRepository trainingRepo, TrainingForRoutineRepository trainingForRoutineRepo, RoutineSequenceUpdateRepository routineSequenceUpdateRepo) {
//        LOG.info("RoutineImpl 생성자 호출");
        this.routineRepo = routineRepo;
        this.trainingRepo = trainingRepo;
        this.trainingForRoutineRepo = trainingForRoutineRepo;
        this.routineSequenceUpdateRepo = routineSequenceUpdateRepo;
    }

    // 루틴 조회
    public List<ResponseRoutineDTO> getRoutines(List<Routine> routines) {
//        LOG.info("getRoutines 호출");
        List<ResponseRoutineDTO> responseRoutineDTOs = new ArrayList<>();
        for (Routine routine : routines) {
            ResponseRoutineDTO responseRoutineDTO = ResponseRoutineDTO.builder()
                    .routineNo(routine.getRoutineNo())
                    .routineName(routine.getRoutineName())
                    .poolLength(routine.getPoolLength())
                    .targetDistance(routine.getTargetDistance())
                    .selStrokes(routine.getSelStrokes())
                    .created(routine.getCreated())
                    .updated(routine.getUpdated())
                    .build();
            responseRoutineDTOs.add(responseRoutineDTO);
        }
        return responseRoutineDTOs;
    }

    // 전체 루틴 목록
    // TODO: UserInfo 추가
    @Override
    public List<ResponseRoutineDTO> getAllRoutines() {
//        LOG.info("getAllRoutines 호출");
        List<Routine> routines = routineRepo.findAll();
        return getRoutines(routines);
    }

    // 유저별 루틴 목록
    @Override
    public List<ResponseRoutineDTO> getSeveralRoutines(UserInfo userInfo) {
//        LOG.info("getSeveralRoutines 호출");
        List<Routine> routines = routineRepo.findByUser(userInfo);
        return getRoutines(routines);
    }

    // 루틴 상세
    @Transactional
    @Override
    public RoutineDetailDTO getRoutineDetail(UserInfo userInfo, Integer routineNo) {
//        LOG.info("getRoutineDetail 호출");
        List<TrainingForRoutineDTO> trainingForRoutineDTOS = new ArrayList<>();

        // 루틴 정보
        RoutineId routineId = RoutineId.builder()
                .routineNo(routineNo)
                .oauthLoginId(userInfo.getEmail())
                .oauthLoginPlatform(userInfo.getPlatform())
                .build();
        Routine routine = routineRepo.getReferenceById(routineId);

        // 루틴에 대한 훈련 정보
        List<TrainingForRoutine> trainingsForRoutine = trainingForRoutineRepo.findAllByRoutineNo(userInfo, routineNo);

        // 루틴에 대한 훈련 정보를 DTO로 변환
        for (TrainingForRoutine trainingForRoutine : trainingsForRoutine) {
            Training training = trainingRepo.getReferenceById(trainingForRoutine.getTrainingId());
            TrainingForRoutineDTO trainingForRoutineDTO = TrainingForRoutineDTO.builder()
                    .trainingId(training.getTrainingId())
                    .session(trainingForRoutine.getSession())
                    .strokeName(training.getStrokeName())
                    .distance(training.getDistance())
                    .sets(training.getSets())
                    .build();
            trainingForRoutineDTOS.add(trainingForRoutineDTO);
        }
        ;

        RoutineDetailDTO routineDetailDTO = RoutineDetailDTO.builder()
                .routineName(routine.getRoutineName())
                .poolLength(routine.getPoolLength())
                .targetDistance(routine.getTargetDistance())
                .selStrokes(routine.getSelStrokes())
                .created(routine.getCreated())
                .updated(routine.getUpdated())
                .trainingData(trainingForRoutineDTOS)
                .build();
//        return trainingForRoutineDTOS;
        return routineDetailDTO;
    }

    // 루틴 저장
    @Override
    public Routine saveRoutine(UserInfo userInfo, RequestRoutineDTO requestRoutineDTO) {
//        LOG.info("saveRoutine 호출");
        int lastRoutineNo = routineRepo.findMaxRoutineNo(userInfo) == null ? 0 : routineRepo.findMaxRoutineNo(userInfo);
        return routineRepo.save(Routine.builder()
                .routineNo(lastRoutineNo + 1)
                .oauthLoginId(userInfo.getEmail())
                .oauthLoginPlatform(userInfo.getPlatform())
                .routineName(requestRoutineDTO.getRoutineName())
                .poolLength(requestRoutineDTO.getPoolLength())
                .targetDistance(requestRoutineDTO.getTargetDistance())
                .selStrokes(requestRoutineDTO.getSelStrokes())
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build()
        );
    }

    // 루틴 수정
    @Override
    public void updateRoutine(UserInfo userInfo, Integer routineNo, RequestRoutineDTO requestRoutineDTO) {
//        LOG.info("RoutineImpl - updateRoutine 호출");
        RoutineId routineId = RoutineId.builder()
                .routineNo(routineNo)
                .oauthLoginId(userInfo.getEmail())
                .oauthLoginPlatform(userInfo.getPlatform())
                .build();
        Routine routine = routineRepo.findById(routineId).orElseThrow();
        routine.change(
                requestRoutineDTO.getRoutineName(),
                requestRoutineDTO.getPoolLength(),
                requestRoutineDTO.getTargetDistance(),
                requestRoutineDTO.getSelStrokes()
        );
        routineRepo.save(routine);
    }

    // 루틴 삭제
    @Override
    public Routine deleteRoutine(@CurrentUser UserInfo userInfo, Integer routineNo) {
//        LOG.info("RoutineImpl - deleteRoutine 호출");
        RoutineId routineId = RoutineId.builder()
                .routineNo(routineNo)
                .oauthLoginId(userInfo.getEmail())
                .oauthLoginPlatform(userInfo.getPlatform())
                .build();

        // routineId로 엔티티 찾기
        Routine routine = routineRepo.findById(routineId).orElseThrow(() ->
                new NoSuchElementException("No routine found with routineNo: " + routineNo)
        );

        // 루틴 삭제
        routineRepo.delete(routine);

        // 프로시저 호출
        routineSequenceUpdateRepo.processRoutineUpdateQueue();

        return routine;
    }
}
