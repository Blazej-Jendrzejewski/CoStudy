package pl.isa.javasmugglers.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.isa.javasmugglers.web.model.*;
import pl.isa.javasmugglers.web.model.user.User;
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

    @GetMapping("examlist/{authToken}")
    String examlist(@PathVariable("authToken") String authToken, Model model) {
        User user = userService.findByAuthToken(authToken);
        model.addAttribute("examlist", examService.listAllExamsByProfessorId(user.getId()))
                .addAttribute("profID", user.getId())
                .addAttribute("content", "examlist")
                .addAttribute("authToken", user.getAuthToken());

        return "main";
    }

    @GetMapping("studentTimetable/{authToken}")
    String studentTimetable(@PathVariable("authToken") String authToken, Model model) {
        User user = userService.findByAuthToken(authToken);
        List<CourseSession> courseSessions = courseService.coursesListByStudentId(user.getId());
        System.out.println(courseSessions);
        model.addAttribute("studentTimetable", courseSessions)
                .addAttribute("content", "studentTimetable")
                .addAttribute("studentId", user.getId())
                .addAttribute("user", user)
                .addAttribute("authToken", authToken);
        return "main";
    }


    @GetMapping("professorTimetable/{authToken}")
    String professorTimetable(@PathVariable("authToken")String authToken, Model model) {
        User user = userService.findByAuthToken(authToken);
        List<Course> courseList = courseService.coursesListByProfessorId(user.getId());
        System.out.println(courseList);
        model.addAttribute("professorCourseList", courseList)
                .addAttribute("content", "professorCourseList")
                .addAttribute("professorId", user.getId())
                .addAttribute("user", user)
                .addAttribute("authToken", authToken);
        return "main";
    }


    @PostMapping("addexam")
    public String addExam(@ModelAttribute Exam exam) {
        examService.saveExam(exam);
        Long activeUserId = exam.getCourseId().getProfessorId().getId();
        return "redirect:/examlist/" + activeUserId;
    }

    @GetMapping("addexam/{authToken}")
    public String showAddExamForm(Model model, @PathVariable("authToken")String authToken) {
        User user = userService.findByAuthToken(authToken);
        model.addAttribute("exam", new Exam())
                .addAttribute("courseList", courseService.coursesListByProfessorId(user.getId()))
                .addAttribute("authToken", authToken)
                .addAttribute("content", "addexam");
        return "main";
    }

    @GetMapping("edit-exam/{encodedID}")
    public String editExam(@PathVariable("encodedID") String encodedID, Model model) {
        Long decodedId = PathEncoderDecoder.decodePath(encodedID);
        Exam exam = examService.findById(decodedId);
        model.addAttribute("exam", exam)
                .addAttribute("courseList",
                        courseService.coursesListByProfessorId(exam.getCourseId().getProfessorId().getId()))
                .addAttribute("content", "editexam");
        return "main";
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
                .addAttribute("content", "questionlist");
        return "main";
    }

    @GetMapping("edit-question/{id}")
    public String editQuestion(@PathVariable("id") Long id, Model model) {
        ExamQuestion examQuestion = examQuestionService.findByID(id);
        model.addAttribute("examQuestion", examQuestion)
                .addAttribute("content", "editquestion");
        return "main";
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
                .addAttribute("alphabet", alphabet)
                .addAttribute("content", "editanswers");


        return "main";
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
        model.addAttribute("exam", exam)
                .addAttribute("content", "addquestion");
        return "main";
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


    @GetMapping("startexam/{authToken}/{examId}")
    public String startExam(@PathVariable Long examId, @PathVariable String authToken, Model model) {
        Exam exam = examService.findById(examId);
        User user = userService.findByAuthToken(authToken);
        UserExamAnswers userExamAnswers = new UserExamAnswers();


        model.addAttribute("exam", exam)
                .addAttribute("examQuestionList", exam.getExamQuestionList())
                .addAttribute("user", user)
                .addAttribute("remainingTime", exam.getDuration())
                .addAttribute("answers", userExamAnswers)
                .addAttribute("content", "exam")
                .addAttribute("authToken", user.getAuthToken());
        return "main";

    }

    @PostMapping("startexam/{authToken}/{examId}")
    public String submitAnswers(@PathVariable Long examId, @PathVariable String authToken,
                                @ModelAttribute UserExamAnswers userExamAnswers) {
        Exam exam = examService.findById(examId);
        User user = userService.findByAuthToken(authToken);
        Double maxScore = examService.calculateExamMaxScore(exam);
        Double userScore = examService.calculateUserScore(userExamAnswers);

        ExamResult examResult = new ExamResult();
        examResult.setExamId(exam);
        examResult.setMaxExamScore(maxScore);
        examResult.setStudentScore(userScore);
        examResult.setStudentId(user);

        examResultService.save(examResult);
        return "redirect:/userexamresults/" + authToken;
    }


    @GetMapping("userexamresults/{authToken}")
    public String showExamResults(Model model, @PathVariable("authToken") String authToken) {
        User user = userService.findByAuthToken(authToken);
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
                .addAttribute("user", user)
                .addAttribute("content", "userexamresults")
                .addAttribute("authToken", user.getAuthToken());
        return "main";
    }


    @GetMapping("/showactiveexams/{authToken}")
    public String showActiveExams(Model model, @PathVariable("authToken") String authToken) {
        User user = userService.findByAuthToken(authToken);
        List<CourseRegistration> registrations = courseRegistrationService.findAllRegisteredCourses(user);
        List<Course> registeredCourses = registrations.stream().map(CourseRegistration::getCourseId).toList();
        List<Exam> allRegisteredExams = examService.findAllByCourseList(registeredCourses);
        List<Exam> takenExams = examResultService.findUserExamResults(user).stream().map(ExamResult::getExamId).toList();

        List<Exam> examsToTake = allRegisteredExams.stream()
                .filter(exam -> exam.getStatus() == Exam.status.ACTIVE &&
                        takenExams.stream().noneMatch(takenExam -> takenExam.getId().equals(exam.getId())))
                .toList();


        model.addAttribute("exams", examsToTake)
                .addAttribute("user", user)
                .addAttribute("content", "userexamlist")
                .addAttribute("authToken", user.getAuthToken());


        return "main";
    }

    @PostMapping("delete/exam/{id}")
    public String deleteExam(@PathVariable("id") Long examID, @RequestParam("userID") Long userID) {
        examService.deleteExam(examID);
        return "redirect:/examlist/" + userID;
    }

    @PostMapping("delete/question/{id}")
    public String deleteQuestion(@PathVariable("id") Long questionID, @RequestParam("examID") Long examID) {
        examAnswerService.deleteAswersByQuestionID(questionID);
        examQuestionService.deleteQuestion(questionID);
        return "redirect:/questionlist/" + examID;
    }


    @GetMapping("/")
    public String home() {
        return "homepage";
    }

    @GetMapping("/registrationsuccesfull")
    public String registrationSuccesfullPage() {
        return "registrationsuccesfull";
    }

    @GetMapping("DashboardProfessor/{authToken}")
    public String professorDashboard(Model model, @PathVariable("authToken") String authToken) {
        User user = userService.findByAuthToken(authToken);
        model.addAttribute("user", user)
                .addAttribute("content", "DashboardProfessor")
                .addAttribute("authToken", user.getAuthToken());
        return "main";
    }

    @GetMapping("user-dashboard/{authToken}")
    public String userDashboard(Model model, @PathVariable("authToken")  String authToken) {
        User user = userService.findByAuthToken(authToken);
        model.addAttribute("user", user)
                .addAttribute("content", "user-dashboard")
                .addAttribute("authToken", user.getAuthToken());
        return "main";
    }


    @GetMapping("/registrationFailed")
    public String registrationFailedPage(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "/rf";
    }


    @GetMapping("user-dashboard/courses/{id}")
    String courselist(@PathVariable("id") Long id, Model model) {
        model.addAttribute("CourseList", examService.listAllExamsByProfessorId(id))
                .addAttribute("profID", id)
                .addAttribute("content", "courseList");

        return "main";
    }



    @GetMapping("/menu")
    public String showMenu() {
        return "menu";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "/login";
    }

}

