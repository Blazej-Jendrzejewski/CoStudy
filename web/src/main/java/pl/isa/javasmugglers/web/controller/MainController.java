package pl.isa.javasmugglers.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.isa.javasmugglers.web.model.*;
import pl.isa.javasmugglers.web.repository.CoursesRepository;
import pl.isa.javasmugglers.web.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Controller
public class MainController {

    @Autowired
    ExamService examService;
    @Autowired
    CourseService courseService;
    @Autowired
    ExamQuestionService examQuestionService;
    @Autowired
    ExamAnswerService examAnswerService;
    @Autowired
    UserService userService;
    @Autowired
    ExamResultService examResultService;
    @Autowired
    CourseRegistrationService courseRegistrationService;



    @GetMapping("examlist/{id}")
    String examlist(@PathVariable("id") Long id, Model model) {
        model.addAttribute("examlist", examService.listAllExamsByProfessorId(id))
                .addAttribute("content", "examlist")
                .addAttribute("profID", id);

        return "examlist";
    }


    @GetMapping("coursesList/{id}")
    String corsesList(@PathVariable("id") Long id, Model model) {
        Collection<Course> courseSessions = courseService.coursesListByProfessorId(id);
        System.out.println(courseSessions);
        model.addAttribute("coursesList", courseSessions)
                .addAttribute("content", "CoursesList")
                .addAttribute("profesorId", id);
        return "coursesList";
    }


    @PostMapping("addexam")
    public String addExam(@ModelAttribute Exam exam) {
        examService.saveExam(exam);
        Long activeUserId = exam.getCourseId().getProfessorId().getId();
        return "redirect:/examlist/" + activeUserId;
    }

    @GetMapping("addexam/{id}")
    public String showAddExamForm(Model model, @PathVariable("id") Long id) {
        model.addAttribute("exam", new Exam())
                .addAttribute("courseList", courseService.coursesListByProfessorId(id))
                .addAttribute("content", "addexam");
        return "addexam";
    }

    @GetMapping("edit-exam/{id}")
    public String editExam(@PathVariable("id") Long id, Model model) {
        Exam exam = examService.findById(id);
        model.addAttribute("exam", exam)
                .addAttribute("courseList",
                        courseService.coursesListByProfessorId(exam.getCourseId().getProfessorId().getId()));
        return "editexam";
    }

    @PostMapping("edit-exam/update-exam/{id}")
    public String updateExam(@PathVariable("id") Long id, @ModelAttribute Exam exam) {
        Exam existingExam = examService.findById(id);
        existingExam.setName(exam.getName());
        existingExam.setDescription(exam.getDescription());
        existingExam.setStatus(exam.getStatus());
        examService.saveExam(exam);
        Long profId = exam.getCourseId().getProfessorId().getId();
        return "redirect:/examlist/" + profId;
    }

    @GetMapping("questionlist/{id}")
    public String questionList(@PathVariable("id") Long id, Model model) {
        List<ExamQuestion> questionList = examQuestionService.findAllQuestionByExamID(id);
        Long profID = examService.findById(id).getCourseId().getProfessorId().getId();
        Long examID = examService.findById(id).getId();
        model.addAttribute("questionList", questionList)
                .addAttribute("profId", profID)
                .addAttribute("examID", examID)
                .addAttribute("content", "questionList");
        return "questionlist";
    }

    @GetMapping("edit-question/{id}")
    public String editQuestion(@PathVariable("id") Long id, Model model) {
        ExamQuestion examQuestion = examQuestionService.findByID(id);
        model.addAttribute("examQuestion", examQuestion);
        return "editquestion";
    }


    @PostMapping("edit-question/update-question/{id}")
    public String updateQuestion(@PathVariable("id") Long id, @ModelAttribute ExamQuestion examQuestion) {
        ExamQuestion existingQuestion = examQuestionService.findByID(id);
        existingQuestion.setQuestionText(examQuestion.getQuestionText());
        existingQuestion.setType(examQuestion.getType());
        examQuestionService.saveQuestion(existingQuestion);
        Long currentExamId = existingQuestion.getExamId().getId();
        return "redirect:/questionlist/" + currentExamId;
    }

    @GetMapping("edit-answers/{id}")
    public String editAnswers(@PathVariable("id") Long id, Model model) {
        ExamQuestion examQuestion = examQuestionService.findByID(id);
        List<ExamAnswer> examAnswerList = examAnswerService.findAllAnswersByQuestionID(id);
        ExamAnswerWrapper examAnswerWrapper = new ExamAnswerWrapper();
        examAnswerWrapper.setExamAnswers(examAnswerList);
        List<Character> alphabet = IntStream.rangeClosed('a', 'z')
                .mapToObj(c -> (char) c)
                .toList();
        model.addAttribute("examQuestion", examQuestion)
                .addAttribute("examAnswers", examAnswerWrapper)
                .addAttribute("alphabet", alphabet);


        return "editanswers";
    }


    @PostMapping("update-answers/{id}")
    public String updateAnswers(@PathVariable("id") Long id, @ModelAttribute("examAnswers") ExamAnswerWrapper examAnswerWrapper) {
        for (ExamAnswer examAnswer : examAnswerWrapper.getExamAnswers()) {
            examAnswerService.saveAnswer(examAnswer);
        }

        ExamQuestion questionID = examAnswerWrapper.getExamAnswers().get(0).getQuestionId();
        Long currentExamID = examService.findByExamQuestion(questionID).getId();

        return "redirect:/questionlist/" + currentExamID;
    }

    @GetMapping("addquestion/{examId}")
    public String showAddQuestionForm(@PathVariable("examId") Long examId, Model model) {
        Exam exam = examService.findById(examId);
        ExamQuestion question = new ExamQuestion();
        question.setExamId(exam);
        model.addAttribute("question", question);
        model.addAttribute("exam", exam);
        return "addquestion";
    }

    @PostMapping("addquestion/{examId}")
    public String saveQuestion(@PathVariable("examId") Long examId, ExamQuestion question, @RequestParam("answers[]") String[] answers, @RequestParam("isCorrect") List<Integer> correctAnswers, Model model) {
        Exam exam = examService.findById(examId);
        question.setExamId(exam);
        examQuestionService.saveQuestion(question);

        for (int i = 0; i < answers.length; i++) {
            ExamAnswer answer = new ExamAnswer();
            answer.setQuestionId(question);
            answer.setAnswerText(answers[i]);
            answer.setCorrect(correctAnswers.contains(i));
            examAnswerService.saveAnswer(answer);
        }

        model.addAttribute("exam", exam);
        return "redirect:/questionlist/" + examId;
    }


    @GetMapping("startexam/{userId}/{examId}")
    public String startExam(@PathVariable Long examId, @PathVariable Long userId, Model model) {
        Exam exam = examService.findById(examId);
        User user = userService.findByID(userId);
        UserExamAnswers userExamAnswers = new UserExamAnswers();


        model.addAttribute("exam", exam)
                .addAttribute("examQuestionList", exam.getExamQuestionList())
                .addAttribute("user", user)
                .addAttribute("remainingTime", exam.getDuration())
                .addAttribute("answers", userExamAnswers);
        return "exam";

    }

    @PostMapping("startexam/{userId}/{examId}")
    public String submitAnswers(@PathVariable Long examId, @PathVariable Long userId,
                                @ModelAttribute UserExamAnswers userExamAnswers) {
        Exam exam = examService.findById(examId);
        User user = userService.findByID(userId);
        Double maxScore = examService.calculateExamMaxScore(exam);
        Double userScore = examService.calculateUserScore(userExamAnswers);

        ExamResult examResult = new ExamResult();
        examResult.setExamId(exam);
        examResult.setMaxExamScore(maxScore);
        examResult.setStudentScore(userScore);
        examResult.setStudentId(user);

        examResultService.save(examResult);
        return "redirect:/userexamresults/" + userId;
    }


    @GetMapping("userexamresults/{userID}")
    public String showExamResults(Model model, @PathVariable("userID") Long userID) {
        User user = userService.findByID(userID);
        List<ExamResult> examResults = examResultService.findUserExamResults(user);
        List<Integer> percentageScores = new ArrayList<>();
        for (ExamResult result : examResults) {
            int percentageScore = examResultService.calculatePercentageScore(
                    result.getStudentScore(),
                    result.getMaxExamScore());
            percentageScores.add(percentageScore);
        }
        model.addAttribute("examResults", examResults)
                .addAttribute("percentageScores", percentageScores)
                .addAttribute("user", user);
        return "userexamresults";
    }


    @GetMapping("/showactiveexams/{userID}")
    public String showActiveExams(Model model, @PathVariable("userID") Long userID) {
        User user = userService.findByID(userID);
        List<CourseRegistration> registrations = courseRegistrationService.findAllRegisteredCourses(user);
        List<Course> registeredCourses = registrations.stream().map(CourseRegistration::getCourseId).toList();
        List<Exam> allRegisteredExams = examService.findAllByCourseList(registeredCourses);
        List<Exam> takenExams = examResultService.findUserExamResults(user).stream().map(ExamResult::getExamId).toList();

        List<Exam> examsToTake = allRegisteredExams.stream()
                .filter(exam -> exam.getStatus() == Exam.status.ACTIVE &&
                        takenExams.stream().noneMatch(takenExam -> takenExam.getId().equals(exam.getId())))
                .toList();


        model.addAttribute("exams", examsToTake)
                .addAttribute("user", user);


        return "userexamlist";
    }

    @GetMapping("user-dashboard/{userID}")
    public String userDashboard(Model model, @PathVariable("userID") Long userID) {
        return "temporary-user-dashboard";
    }

    @GetMapping("user-dashboard/courses/{id}")
    String courselist(@PathVariable("id") Long id, Model model) {
        model.addAttribute("CourseList", examService.listAllExamsByProfessorId(id))
                .addAttribute("content", "CourseList")
                .addAttribute("profID", id);

        return "CourseList";
    }

    @GetMapping("/menu")
    public String showMenu() {
        return "menu";
    }

//    @GetMapping("/1")
//    public String executeOption1(Model model) {
//        List<Course> courses = courseRepository.findAll();
//        model.addAttribute("courses", courses);
//        return "result";
//    }
}