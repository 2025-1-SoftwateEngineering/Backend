package com.example.vocabook.domain.voca.service;

import com.example.vocabook.domain.member.converter.MemberConverter;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberChoice;
import com.example.vocabook.domain.member.entity.mapping.MemberCrossword;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.member.repository.*;
import com.example.vocabook.domain.member.entity.mapping.MemberWord;
import com.example.vocabook.domain.voca.converter.VocaConverter;
import com.example.vocabook.domain.voca.dto.VocaReqDTO;
import com.example.vocabook.domain.voca.dto.VocaResDTO;
import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.entity.mapping.ChoiceQuestion;
import com.example.vocabook.domain.voca.entity.mapping.CrosswordHint;
import com.example.vocabook.domain.voca.enums.ClueType;
import com.example.vocabook.domain.voca.exception.VocaException;
import com.example.vocabook.domain.voca.code.VocaErrorCode;
import com.example.vocabook.domain.voca.repository.*;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.apiPayload.converter.PagingConverter;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.hibernate.dialect.BooleanDecoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VocaService {

	private final VocaRepository vocaRepository;
	private final WordRepository wordRepository;
	private final MemberVocaRepository memberVocaRepository;
	private final MemberRepository memberRepository;
    private final ChoiceRepository choiceRepository;
    private final ChoiceQuestionRepository choiceQuestionRepository;
    private final MemberChoiceRepository memberChoiceRepository;
	private final MemberWordRepository memberWordRepository;
    private final RedisUtil redisUtil;
    private final CrosswordRepository crosswordRepository;
    private final MemberCrosswordRepository memberCrosswordRepository;
    private final CrosswordHintRepository crosswordHintRepository;

    // 단어장 전체 목록 조회
	@Transactional(readOnly = true)
	public VocaResDTO.VocaList getVocaList(AuthMember authMember) {
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		List<Voca> vocas = vocaRepository.findAll();
		List<Word> allWords = wordRepository.findByVocaIn(vocas);
		Map<Long, Long> wordCountMap = allWords.stream()
				.collect(java.util.stream.Collectors.groupingBy(w -> w.getVoca().getId(), java.util.stream.Collectors.counting()));
		Map<Long, Long> memorizedCountMap = memberWordRepository.countByMemberGroupByVoca(member, vocas).stream()
				.collect(java.util.stream.Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
		return VocaConverter.toVocaList(vocas, wordCountMap, memorizedCountMap);
	}

	// 암기한 단어 저장
	@Transactional
	public VocaResDTO.MemorizeInfo memorizeWords(Long vocaId, AuthMember authMember, VocaReqDTO.Memorize dto) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		List<Word> words = wordRepository.findAllById(dto.getWordIds());

		java.util.Set<Long> alreadyMemorized = memberWordRepository.findWordIdsByMemberAndWordIn(member, words);
		words.stream()
				.filter(w -> !alreadyMemorized.contains(w.getId()))
				.forEach(w -> memberWordRepository.save(MemberWord.builder().member(member).word(w).build()));

		List<Long> allMemorizedIds = memberWordRepository.findWordIdsByMemberAndVocaId(member, vocaId);
		return VocaResDTO.MemorizeInfo.builder()
				.memorizedWordIds(allMemorizedIds)
				.totalCount(allMemorizedIds.size())
				.build();
	}

	// 암기한 단어 목록 조회
	@Transactional(readOnly = true)
	public VocaResDTO.MemorizeInfo getMemorizedWords(Long vocaId, AuthMember authMember) {
		vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		List<Long> memorizedWordIds = memberWordRepository.findWordIdsByMemberAndVocaId(member, vocaId);
		return VocaResDTO.MemorizeInfo.builder()
				.memorizedWordIds(memorizedWordIds)
				.totalCount(memorizedWordIds.size())
				.build();
	}

	// 학습한 단어장 목록 조회
	@Transactional(readOnly = true)
	public VocaResDTO.StudiedVocaList getStudiedVocas(AuthMember authMember) {
		Member member = authMember.getMember();
		List<MemberVoca> memberVocas = memberVocaRepository.findAllByMember(member);
		return VocaConverter.toStudiedVocaList(memberVocas);
	}

    public VocaResDTO.WordList getWords(Long vocaId, int page, int pageSize) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		Page<Word> wordPage = wordRepository.findByVoca(voca, PageRequest.of(page, pageSize));
		return VocaConverter.toWordList(voca, wordPage);
	}

	public List<VocaResDTO.TestQuestion> getTestQuestions(Long vocaId) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		List<Word> words = new ArrayList<>(wordRepository.findByVoca(voca));
		Collections.shuffle(words);
		return words.stream()
				.map(VocaConverter::toTestQuestion)
				.toList();
	}

	// 단어 하나씩 즉시 채점
	@Transactional(readOnly = true)
	public VocaResDTO.AnswerResult submitAnswer(Long vocaId, VocaReqDTO.SubmitAnswer dto) {
		vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		Word word = wordRepository.findById(dto.getWordId())
				.orElseThrow(() -> new VocaException(VocaErrorCode.WORD_NOT_FOUND));
		boolean isCorrect = word.getEnglishWord().equalsIgnoreCase(dto.getAnswer());
		return VocaResDTO.AnswerResult.builder()
				.wordId(word.getId())
				.meaning(word.getMeaning())
				.correctAnswer(word.getEnglishWord())
				.submittedAnswer(dto.getAnswer())
				.correct(isCorrect)
				.build();
	}

	// 테스트 전체 완료 - 코인/스트릭 처리
	@Transactional
	public VocaResDTO.TestResult completeTest(Long vocaId, AuthMember authMember, VocaReqDTO.CompleteTest dto) {
		Voca voca = vocaRepository.findById(vocaId)
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));
		Member member = memberRepository.findById(authMember.getMember().getId())
				.orElseThrow(() -> new VocaException(VocaErrorCode.VOCA_NOT_FOUND));

		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		boolean alreadySubmittedToday = memberVocaRepository.findByMemberAndVoca(member, voca)
				.map(mv -> mv.getSolvedAt() != null && mv.getSolvedAt().toLocalDate().isEqual(today))
				.orElse(false);

		List<Long> wordIds = dto.getAnswers().stream()
				.map(VocaReqDTO.CompleteTest.Answer::getWordId)
				.toList();
		Map<Long, Word> wordMap = wordRepository.findAllById(wordIds).stream()
				.collect(java.util.stream.Collectors.toMap(Word::getId, w -> w));

		List<VocaResDTO.AnswerResult> results = new ArrayList<>();
		int correctCount = 0;

		for (VocaReqDTO.CompleteTest.Answer answer : dto.getAnswers()) {
            Word word = wordRepository.findById(answer.getWordId())
                    .orElseThrow(() -> new VocaException(VocaErrorCode.WORD_NOT_FOUND));
        }
		for (VocaReqDTO.CompleteTest.Answer answer : dto.getAnswers()) {
			Word word = Optional.ofNullable(wordMap.get(answer.getWordId()))
					.orElseThrow(() -> new VocaException(VocaErrorCode.WORD_NOT_FOUND));
			boolean isCorrect = word.getEnglishWord().equalsIgnoreCase(answer.getAnswer());
			if (isCorrect) correctCount++;

			results.add(VocaResDTO.AnswerResult.builder()
					.wordId(word.getId())
					.meaning(word.getMeaning())
					.correctAnswer(word.getEnglishWord())
					.submittedAnswer(answer.getAnswer())
					.correct(isCorrect)
					.build());
		}

		long earnedCoins = 0;
		if (!alreadySubmittedToday) {
			earnedCoins = (long) correctCount * 5;
			member.addCoin(earnedCoins);
			member.updateStreak();
			if (member.getStreak() % 7 == 0) {
				member.addCoin(500);
				earnedCoins += 500;
			}
		} else {
			earnedCoins = (long) correctCount * 3;
			member.addCoin(earnedCoins);
		}
		memberRepository.saveAndFlush(member);
		saveMemberVoca(member, voca, (long) correctCount, (long) dto.getAnswers().size());

		return VocaResDTO.TestResult.builder()
				.totalCount(dto.getAnswers().size())
				.correctCount(correctCount)
				.wrongCount(dto.getAnswers().size() - correctCount)
				.earnedCoins(earnedCoins)
				.results(results)
				.build();
	}

	private void saveMemberVoca(Member member, Voca voca, Long correctCnt, Long learningWordCnt) {
		memberVocaRepository.findByMemberAndVoca(member, voca)
				.ifPresentOrElse(
						memberVoca -> memberVoca.updateResult(correctCnt, learningWordCnt, LocalDateTime.now()),
						() -> memberVocaRepository.save(MemberVoca.builder()
								.member(member)
								.voca(voca)
								.correctCnt(correctCnt)
								.learningWordCnt(learningWordCnt)
								.solvedAt(LocalDateTime.now())
								.build())
				);
	}

    // 사지선다 문제 목록 조회
    public PagingResDTO.Cursor<VocaResDTO.GetChoiceList> getChoiceList(
            AuthMember auth,
            String cursor,
            Integer pageSize
    ) {
        // Pageable
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        Slice<Choice> choiceList;
        if (cursor.equals("-1")) {
            choiceList = choiceRepository.findChoiceWithoutCursor(pageRequest);
        } else {
            Long idCursor;
            try {
                idCursor = Long.parseLong(cursor);
            } catch (NumberFormatException e) {
                throw new VocaException(GeneralErrorCode.INVADED_CURSOR);
            }

            choiceList = choiceRepository.findChoiceWithCursor(idCursor, pageRequest);
        }

        // 사지선다 문제가 없는 경우
        if (!choiceList.hasContent()){
            return PagingConverter.toCursor(null, null, false, 0);
        }

        // 다음 커서 제작
        String nextCursor = choiceList.getContent().getLast().getId().toString();

        // 사용자가 플레이한 횟수
        return PagingConverter.toCursor(
                choiceList.getContent().stream()
                        .map(c -> VocaConverter.toGetChoiceList(
                                c,
                                memberChoiceRepository.countByMemberAndChoice(auth.getMember(), c)
                        ))
                        .toList(),
                nextCursor,
                choiceList.hasNext(),
                choiceList.getNumberOfElements()
        );
    }

    // 사지선다 선택지 조회
    @Transactional
    public VocaResDTO.GetChoice getChoice(
            Long choiceId,
            Long current,
            AuthMember auth
    ) {

        // 사지선다 존재여부 확인
        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new VocaException(VocaErrorCode.CHOICE_NOT_FOUND));

        // 처음이라면 current = 0
        current = current == null ? 0L : current;

        // 사지선다에 포함되고 현재 진행중인 단어 이후 단어 조회
        Optional<ChoiceQuestion> choiceQuestion = choiceQuestionRepository.findByChoiceWithCurrent(choice.getId(), current);

        if (choiceQuestion.isEmpty()) {
            return null;
        }

        MemberChoice memberChoice = memberChoiceRepository.findByMemberAndChoiceAndSolvedAtIsNull(auth.getMember(), choice)
                .orElseGet(() -> memberChoiceRepository.save(MemberConverter.toMemberChoice(auth.getMember(), choice)));

        // 사지선다 구성 (ID와 텍스트 쌍으로 구성)
        String question;
        List<VocaResDTO.ChoiceElement> choiceElements = new ArrayList<>();
        if (choiceQuestion.get().getIsWord()){
            question = choiceQuestion.get().getWord().getEnglishWord();

            // 정답 삽입
            choiceElements.add(VocaConverter.toChoiceElement(
                    choiceQuestion.get().getWord().getId(),
                    choiceQuestion.get().getWord().getMeaning())
            );

            // 나머지 3개 삽입
            List<Word> randomWords = wordRepository.findRandomExcluding(choiceQuestion.get().getWord().getId(), 3);
            randomWords.forEach(w -> choiceElements.add(VocaConverter.toChoiceElement(
                    w.getId(),
                    w.getMeaning()))
            );
        } else {
            question = choiceQuestion.get().getWord().getMeaning();

            // 정답 삽입
            choiceElements.add(VocaConverter.toChoiceElement(
                    choiceQuestion.get().getWord().getId(),
                    choiceQuestion.get().getWord().getEnglishWord())
            );

            // 나머지 3개 삽입
            List<Word> randomWords = wordRepository.findRandomExcluding(choiceQuestion.get().getWord().getId(), 3);
            randomWords.forEach(w -> choiceElements.add(VocaConverter.toChoiceElement(
                    w.getId(),
                    w.getEnglishWord()))
            );
        }
        Collections.shuffle(choiceElements);

        // Redis에 시간 삽입 (폴링 시간 계산 +1초 허용)
        // Redis Key 구조: 사용자 ID:사지선다 ID:사지선다 항목 ID
        redisUtil.save(
                auth.getMember().getId()+":"+choice.getId().toString()+":"+choiceQuestion.get().getId(),
                LocalDateTime.now(ZoneId.of("Asia/Seoul")),
                Duration.ofSeconds(6)
        );

        return VocaConverter
                .toGetChoice(
                        memberChoice.getId(),
                        memberChoice.getScore(),
                        question,
                        choiceElements
                        );
    }

    // 사지선다 정답 제출 / current = 현재 푼 ChoiceQuestion ID
    @Transactional
    public VocaResDTO.SubmitChoice submitChoice(
            AuthMember auth,
            Long choiceId,
            Long answer,
            Long current
    ) {

        Choice choice = choiceRepository.findById(choiceId)
                .orElseThrow(() -> new VocaException(VocaErrorCode.CHOICE_NOT_FOUND));

        // 사용자가 이 사지선다를 플레이하고 있는지 확인
        MemberChoice memberChoice = memberChoiceRepository.findByMemberAndChoiceAndSolvedAtIsNull(auth.getMember(), choice)
                .orElseThrow(() -> new VocaException(VocaErrorCode.NOT_PLAY_CHOICE));

        ChoiceQuestion choiceQuestion = choiceQuestionRepository
                .findByChoiceWithCurrent(memberChoice.getChoice().getId(), current)
                .orElseThrow(() -> new VocaException(VocaErrorCode.NOT_PLAY_CHOICE));

        // 가장 먼저 Redis에서 시간 꺼내오기
        String redisKey = auth.getMember().getId()+":"+memberChoice.getChoice().getId().toString()+":"+choiceQuestion.getId();
        LocalDateTime submitTime = null;
        if (redisUtil.hasKey(redisKey)){
            submitTime = (LocalDateTime) redisUtil.get(redisKey);
        }

        // 사용자랑 맞는지 검증
        if (!memberChoice.getMember().getId().equals(auth.getMember().getId())) {
            throw new VocaException(VocaErrorCode.CHOICE_MISMATCH_MEMBER);
        }

        // 요청한 사지선다가 이미 완료한 사지선다인 경우
        if (memberChoice.getSolvedAt() != null) {
            throw new VocaException(VocaErrorCode.CHOICE_ALREADY_CLEAR);
        }

        // 사지선다 채점 로직
        Boolean isCorrect = Boolean.FALSE;
        Long userScore = memberChoice.getScore();

        // 답변이 존재하고 Redis에 시작 시간이 기록된 경우에만 정답 체크
        if (answer != null && answer > 0 && submitTime != null) {
            Word answerWord = wordRepository.findById(answer).orElse(null);

            if (answerWord != null && choiceQuestion.getWord().getId().equals(answerWord.getId())) {
                // 스코어 계산 (5초 기준)
                long remain = Duration.between(submitTime, LocalDateTime.now(ZoneId.of("Asia/Seoul"))).getSeconds();

                Long score = switch ((int) (5 - remain)) {
                    case 5, 4 -> 1000L;
                    case 3 -> 800L;
                    case 2 -> 600L;
                    case 1 -> 400L;
                    case 0 -> 200L;
                    default -> 0L;
                };
                memberChoice.correct(score);
                userScore += score;
                isCorrect = Boolean.TRUE;
            }
        }

        if (!isCorrect) {
            memberChoice.wrong();
        }

        // 지금 푼 사지선다가 마지막인 경우 (다음 문제가 없는지 확인)
        Optional<ChoiceQuestion> nextQuestion = choiceQuestionRepository.findNextQuestion(choiceQuestion.getId());
        if (nextQuestion.isEmpty()){

            // 지금까지 푼 스코어가 최다 득점인지
            if (memberChoice.getMember().getChoiceHigher().compareTo(userScore) < 0){
                memberChoice.getMember().updateChoiceHigher(userScore);
            }

            // 초회인 경우 재화 지급
            if (memberChoiceRepository.countByMemberAndChoice(auth.getMember(), choice).equals(1L)) {
                memberChoice.getMember().addCoin(choiceQuestion.getChoice().getSolvedCoin());
            }

            // 사지선다 완료 처리
            memberChoice.end();
        }

        if (nextQuestion.isPresent()) {
            return VocaConverter.toSubmitChoice(isCorrect, true, choiceQuestion.getId(), userScore);
        } else {
            return VocaConverter.toSubmitChoice(isCorrect, false, null, userScore);
        }
    }

    // 십자말풀이 목록 조회
    public PagingResDTO.Cursor<VocaResDTO.GetCrosswordList> getCrosswordList(
            AuthMember auth,
            String cursor,
            Integer pageSize
    ) {
        // Pageable
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        Slice<Crossword> crosswordList;
        if (cursor.equals("-1")) {
            crosswordList = crosswordRepository.findCrosswordWithoutCursor(pageRequest);
        } else {
            Long idCursor;
            try {
                idCursor = Long.parseLong(cursor);
            } catch (NumberFormatException e) {
                throw new VocaException(GeneralErrorCode.INVADED_CURSOR);
            }

            crosswordList = crosswordRepository.findCrosswordWithCursor(idCursor, pageRequest);
        }

        // 사지선다 문제가 없는 경우
        if (!crosswordList.hasContent()){
            return PagingConverter.toCursor(null, null, false, 0);
        }

        // 다음 커서 제작
        String nextCursor = crosswordList.getContent().getLast().getId().toString();

        // 사용자가 플레이한 횟수
        return PagingConverter.toCursor(
                crosswordList.getContent().stream()
                        .map(c -> VocaConverter.toGetCrosswordList(
                                c,
                                memberCrosswordRepository.countByMemberAndCrossword(auth.getMember(), c)
                        ))
                        .toList(),
                nextCursor,
                crosswordList.hasNext(),
                crosswordList.getNumberOfElements()
        );
    }

    // 십자말풀이 문제 조회
    @Transactional
    public VocaResDTO.GetCrossword getCrossword(
            Long crosswordId,
            AuthMember auth
    ) {
        // 사용자 십자말풀이 생성
        Crossword crossword = crosswordRepository.findById(crosswordId)
                .orElseThrow(() -> new VocaException(VocaErrorCode.CROSSWORD_NOT_FOUND));

        MemberCrossword memberCrossword = memberCrosswordRepository
                .findByMemberAndCrosswordAndSolvedAtIsNull(auth.getMember(), crossword)
                .orElseGet(() -> memberCrosswordRepository
                        .save(MemberConverter.toMemberCross(auth.getMember(), crossword)));

        // 응답으로 NXN 형태의 데이터로 전달
        // 초기화
        List<CrosswordHint> crosswordHintList = crosswordHintRepository.findAllByCrossword(crossword);

        // 가로 세로 가장 긴 부분 결정
        Integer maxN = getInteger(crosswordHintList);

        // 최대 N 기준 초기화
        List<String[]> result = new ArrayList<>();

        // 빈칸으로 일단 초기화
        for (int i = 0; i < maxN; i++) {
            result.add(new String[maxN]);
        }

        // 시작 시간 Redis에 저장
        // 키 = 사용지 ID:사용자 십자말풀이 ID / 만료시간 X
        String redisKey = auth.getMember().getId()+":"+memberCrossword.getId().toString();
        redisUtil.save(redisKey, LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        return VocaConverter.toGetCrossword(
                maxN,
                crosswordHintList.stream()
                        .map(ch -> VocaConverter.toCrosswordElement(
                                ch.getId(),
                                ch.getClueType(),
                                ch.getWord().getEnglishWord().length(),
                                ch.getClueContent(),
                                Integer.parseInt(ch.getWordStartPoint().split(" ")[0])-1,
                                Integer.parseInt(ch.getWordStartPoint().split(" ")[1])-1
                        ))
                        .toList()
        );
    }

    private static Integer getInteger(List<CrosswordHint> crosswordHintList) {
        Integer maxN = 0;
        for (CrosswordHint ch : crosswordHintList) {
            String[] splitStartPoint = ch.getWordStartPoint().split(" ");
            Integer verticalPoint = Integer.parseInt(splitStartPoint[0])-1;
            Integer horizontalPoint = Integer.parseInt(splitStartPoint[1])-1;

            if (ch.getClueType().equals(ClueType.ACROSS)) {
                // 가로 최대
                maxN = Math.max(maxN, horizontalPoint+ch.getWord().getEnglishWord().length());
            } else {
                // 세로 최대
                maxN = Math.max(maxN, verticalPoint+ch.getWord().getEnglishWord().length());
            }
        }
        return maxN;
    }

    // 십자말풀이 정답 제출
    @Transactional
    public VocaResDTO.SubmitCrossword submitCrossword(
            Long crosswordId,
            AuthMember auth,
            Long crosswordHintId,
            String answer
    ) {
        // 지금 시각 빠르게 확정
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 사용자 십자말풀이 조회
        Crossword crossword = crosswordRepository.findById(crosswordId)
                .orElseThrow(() -> new VocaException(VocaErrorCode.CROSSWORD_NOT_FOUND));

        MemberCrossword memberCrossword = memberCrosswordRepository
                .findByMemberAndCrosswordAndSolvedAtIsNull(auth.getMember(), crossword)
                .orElseThrow(() -> new VocaException(VocaErrorCode.NOT_PLAY_CROSS));

        // 이미 완료한 십자말풀이인지 확인
        if (memberCrossword.getSolvedAt() != null) {
            throw new VocaException(VocaErrorCode.CROSSWORD_ALREADY_CLEAR);
        }

        // Redis 저장된 시간 꺼내기
        String redisKey = auth.getMember().getId()+":"+memberCrossword.getId().toString();
        LocalDateTime submitTime = (LocalDateTime) redisUtil.get(redisKey);

        // 십자말풀이 정답 채점
        CrosswordHint crosswordHint = crosswordHintRepository.findById(crosswordHintId)
                .orElseThrow(() -> new VocaException(VocaErrorCode.CROSSWORD_NOT_FOUND));

        Word answerWord = wordRepository.findByEnglishWord(answer)
                .orElseThrow(() -> new VocaException(VocaErrorCode.WORD_NOT_FOUND));

        Boolean isCorrect = Boolean.FALSE;
        Long correctCnt = memberCrossword.getCorrectCnt();
        Duration solvingTime = memberCrossword.getSolvingTime();
        if (crosswordHint.getWord().getId().equals(answerWord.getId())){
            isCorrect = Boolean.TRUE;
            memberCrossword.correct(Duration.between(submitTime, now));

            // 정답이였다면 바로 보정
            solvingTime = solvingTime.plus(Duration.between(submitTime, now));

            // Redis에 저장되어 있는 정보 삭제 -> 다시 저장
            redisUtil.delete(redisKey);
            redisUtil.save(redisKey, now);
            correctCnt++;
        }

        // 십자말풀이 완료 검증
        // 지금까지 십자말풀이 정답 맞춘 개수와 비교
        Long crosswordHintCnt = crosswordHintRepository.countByCrossword(crossword);
        Long memberCrosswordCnt = memberCrosswordRepository.countByMemberAndCrossword(auth.getMember(), crossword);
        Boolean hasNext = Boolean.TRUE;
        if (correctCnt.equals(crosswordHintCnt)){

            hasNext = Boolean.FALSE;

            // 최단기간인지 확인
            if (memberCrossword.getMember().getCrosswordHigher().equals(Duration.ZERO) ||
                    memberCrossword.getMember().getCrosswordHigher().compareTo(solvingTime) > 0
            ){
                memberCrossword.getMember().updateCrosswordHigher(solvingTime);
            }

            // 초회 클리어인지 확인
            if (memberCrosswordCnt.equals(1L)){
                memberCrossword.getMember().addCoin(crossword.getSolvedCoin());
            }

            // 완료 처리
            memberCrossword.end();
        }

        // 응답
        return VocaConverter.toSubmitCrossword(isCorrect, hasNext, solvingTime);
    }
}
