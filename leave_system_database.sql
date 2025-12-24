-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: leave_system
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `absence`
--

DROP TABLE IF EXISTS `absence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `absence` (
  `absence_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `offering_id` bigint NOT NULL,
  `course_date` date NOT NULL,
  `section_start` tinyint NOT NULL,
  `section_end` tinyint NOT NULL,
  `source` varchar(16) NOT NULL DEFAULT 'TEACHER',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING_MAKEUP',
  `makeup_deadline` datetime DEFAULT NULL,
  `converted_leave_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`absence_id`),
  KEY `fk_absence_student` (`student_id`),
  KEY `fk_absence_offering` (`offering_id`),
  KEY `fk_absence_leave` (`converted_leave_id`),
  CONSTRAINT `fk_absence_leave` FOREIGN KEY (`converted_leave_id`) REFERENCES `leave_request` (`leave_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_absence_offering` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_absence_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `absence`
--

LOCK TABLES `absence` WRITE;
/*!40000 ALTER TABLE `absence` DISABLE KEYS */;
/*!40000 ALTER TABLE `absence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `approval`
--

DROP TABLE IF EXISTS `approval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval` (
  `approval_id` bigint NOT NULL AUTO_INCREMENT,
  `leave_id` bigint NOT NULL,
  `approver_id` bigint NOT NULL,
  `approver_role` varchar(16) NOT NULL,
  `action` varchar(16) NOT NULL,
  `comment` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`approval_id`),
  KEY `fk_approval_leave` (`leave_id`),
  KEY `fk_approval_staff` (`approver_id`),
  CONSTRAINT `fk_approval_leave` FOREIGN KEY (`leave_id`) REFERENCES `leave_request` (`leave_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_approval_staff` FOREIGN KEY (`approver_id`) REFERENCES `staff` (`staff_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approval`
--

LOCK TABLES `approval` WRITE;
/*!40000 ALTER TABLE `approval` DISABLE KEYS */;
/*!40000 ALTER TABLE `approval` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attendance_session`
--

DROP TABLE IF EXISTS `attendance_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_session` (
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL,
  `offering_id` bigint NOT NULL,
  `course_date` date NOT NULL,
  `section_start` int NOT NULL,
  `section_end` int NOT NULL,
  `token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `token_expire_time` datetime NOT NULL,
  `allow_start_time` datetime NOT NULL,
  `allow_end_time` datetime NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`session_id`),
  KEY `fk_session_teacher` (`teacher_id`),
  KEY `fk_session_offering` (`offering_id`),
  CONSTRAINT `fk_session_offering` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`),
  CONSTRAINT `fk_session_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attendance_session`
--

LOCK TABLES `attendance_session` WRITE;
/*!40000 ALTER TABLE `attendance_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `attendance_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class`
--

DROP TABLE IF EXISTS `class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class` (
  `class_id` bigint NOT NULL AUTO_INCREMENT,
  `class_code` varchar(32) NOT NULL,
  `class_name` varchar(64) NOT NULL,
  `major` varchar(64) DEFAULT NULL,
  `grade_year` int NOT NULL,
  `counselor_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_code` (`class_code`),
  KEY `fk_class_counselor` (`counselor_id`),
  CONSTRAINT `fk_class_counselor` FOREIGN KEY (`counselor_id`) REFERENCES `staff` (`staff_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class`
--

LOCK TABLES `class` WRITE;
/*!40000 ALTER TABLE `class` DISABLE KEYS */;
INSERT INTO `class` VALUES (1,'CS2101','计科21-1班','计算机科学与技术',2021,1,'2025-12-08 23:09:39','2025-12-08 23:09:39');
/*!40000 ALTER TABLE `class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `course_id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(32) NOT NULL,
  `course_name` varchar(100) NOT NULL,
  `credit` decimal(4,1) NOT NULL DEFAULT '0.0',
  `total_hours` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `course_code` (`course_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'CS101','高等数学A',4.0,64,'2025-12-08 23:09:39','2025-12-08 23:09:39');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enrollment`
--

DROP TABLE IF EXISTS `enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enrollment` (
  `enrollment_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `offering_id` bigint NOT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'ENROLLED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uk_enrollment_student_offering` (`student_id`,`offering_id`),
  KEY `fk_enrollment_offering` (`offering_id`),
  CONSTRAINT `fk_enrollment_offering` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enrollment`
--

LOCK TABLES `enrollment` WRITE;
/*!40000 ALTER TABLE `enrollment` DISABLE KEYS */;
INSERT INTO `enrollment` VALUES (1,1,1,'ENROLLED','2025-12-08 23:09:39','2025-12-08 23:09:39'),(2,2,1,'ENROLLED','2025-12-08 23:09:39','2025-12-08 23:09:39');
/*!40000 ALTER TABLE `enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leave_impact`
--

DROP TABLE IF EXISTS `leave_impact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_impact` (
  `impact_id` bigint NOT NULL AUTO_INCREMENT,
  `leave_id` bigint NOT NULL,
  `offering_id` bigint NOT NULL,
  `course_date` date NOT NULL,
  `section_start` tinyint NOT NULL,
  `section_end` tinyint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `confirm_status` varchar(16) NOT NULL DEFAULT 'PENDING',
  `confirm_time` datetime DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`impact_id`),
  KEY `fk_impact_leave` (`leave_id`),
  KEY `fk_impact_offering` (`offering_id`),
  KEY `fk_impact_teacher` (`teacher_id`),
  CONSTRAINT `fk_impact_leave` FOREIGN KEY (`leave_id`) REFERENCES `leave_request` (`leave_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_impact_offering` FOREIGN KEY (`offering_id`) REFERENCES `offering` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_impact_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `staff` (`staff_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leave_impact`
--

LOCK TABLES `leave_impact` WRITE;
/*!40000 ALTER TABLE `leave_impact` DISABLE KEYS */;
INSERT INTO `leave_impact` VALUES (1,1,1,'2024-10-10',1,2,2,'PENDING',NULL,NULL);
/*!40000 ALTER TABLE `leave_impact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `leave_request`
--

DROP TABLE IF EXISTS `leave_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `leave_request` (
  `leave_id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `term_id` bigint NOT NULL,
  `leave_type` varchar(16) NOT NULL,
  `apply_channel` varchar(16) NOT NULL DEFAULT 'BY_COURSE',
  `reason` varchar(500) NOT NULL,
  `proof_url` varchar(255) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `rejected_at` datetime DEFAULT NULL,
  `cancelled_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`leave_id`),
  KEY `fk_leave_student` (`student_id`),
  KEY `fk_leave_term` (`term_id`),
  CONSTRAINT `fk_leave_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_leave_term` FOREIGN KEY (`term_id`) REFERENCES `term` (`term_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `leave_request`
--

LOCK TABLES `leave_request` WRITE;
/*!40000 ALTER TABLE `leave_request` DISABLE KEYS */;
INSERT INTO `leave_request` VALUES (1,1,1,'SICK','BY_COURSE','感冒发烧，需要到医院就诊',NULL,'2024-10-10 08:00:00','2024-10-10 12:00:00','PENDING_COUNSELOR',NULL,NULL,'2025-12-23 00:35:04','2025-12-23 00:35:04');
/*!40000 ALTER TABLE `leave_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login_token`
--

DROP TABLE IF EXISTS `login_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `login_token` (
  `token_id` bigint NOT NULL AUTO_INCREMENT,
  `user_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `role_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expire_time` datetime NOT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `token` (`token`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login_token`
--

LOCK TABLES `login_token` WRITE;
/*!40000 ALTER TABLE `login_token` DISABLE KEYS */;
INSERT INTO `login_token` VALUES (1,'STUDENT',1,'STUDENT','c54f8ad7b5c44520918b74d0c0a3648e','2025-12-24 00:00:57','2025-12-23 22:00:57'),(2,'STAFF',1,'COUNSELOR','d844bc6a63ec464db3453f2868e39925','2025-12-24 00:12:38','2025-12-23 22:12:38'),(3,'STUDENT',1,'STUDENT','a01b704ceb7545eb88165f5126240b8a','2025-12-24 02:35:25','2025-12-24 00:35:25'),(4,'STUDENT',1,'STUDENT','95adcd6346ec4117ba816ca72f3689b7','2025-12-24 02:35:35','2025-12-24 00:35:35'),(5,'STAFF',1,'COUNSELOR','12bf284086b64e1c8bf5ee858c26f0a3','2025-12-24 02:35:43','2025-12-24 00:35:43');
/*!40000 ALTER TABLE `login_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `offering`
--

DROP TABLE IF EXISTS `offering`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `offering` (
  `offering_id` bigint NOT NULL AUTO_INCREMENT,
  `term_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `class_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `week_day` tinyint NOT NULL,
  `section_start` tinyint NOT NULL,
  `section_end` tinyint NOT NULL,
  `classroom` varchar(64) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`offering_id`),
  KEY `fk_offering_term` (`term_id`),
  KEY `fk_offering_course` (`course_id`),
  KEY `fk_offering_class` (`class_id`),
  KEY `fk_offering_teacher` (`teacher_id`),
  CONSTRAINT `fk_offering_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`class_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_offering_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_offering_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `staff` (`staff_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_offering_term` FOREIGN KEY (`term_id`) REFERENCES `term` (`term_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offering`
--

LOCK TABLES `offering` WRITE;
/*!40000 ALTER TABLE `offering` DISABLE KEYS */;
INSERT INTO `offering` VALUES (1,1,1,1,2,1,1,2,'一教-101','2025-12-08 23:09:39','2025-12-08 23:09:39');
/*!40000 ALTER TABLE `offering` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `staff_id` bigint NOT NULL AUTO_INCREMENT,
  `staff_no` varchar(32) NOT NULL,
  `name` varchar(50) NOT NULL,
  `gender` char(1) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `staff_no` (`staff_no`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,'T2024001','张辅导','F','13800000001','counselor@example.com',1,'2025-12-08 23:09:39','2025-12-08 23:09:39',NULL),(2,'T2024002','李老师','M','13800000002','teacher@example.com',1,'2025-12-08 23:09:39','2025-12-08 23:09:39',NULL);
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff_role`
--

DROP TABLE IF EXISTS `staff_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `staff_id` bigint NOT NULL,
  `role_code` varchar(32) NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_staff_role_staff` (`staff_id`),
  CONSTRAINT `fk_staff_role_staff` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff_role`
--

LOCK TABLES `staff_role` WRITE;
/*!40000 ALTER TABLE `staff_role` DISABLE KEYS */;
INSERT INTO `staff_role` VALUES (1,1,'COUNSELOR','辅导员'),(2,2,'TEACHER','授课教师');
/*!40000 ALTER TABLE `staff_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `student_id` bigint NOT NULL AUTO_INCREMENT,
  `student_no` varchar(32) NOT NULL,
  `name` varchar(50) NOT NULL,
  `gender` char(1) DEFAULT NULL,
  `class_id` bigint NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `status` varchar(16) NOT NULL DEFAULT 'NORMAL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `password` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `student_no` (`student_no`),
  KEY `fk_student_class` (`class_id`),
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`class_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,'20210001','小明','M',1,'13900000001','xm@example.com','NORMAL','2025-12-08 23:09:39','2025-12-23 21:00:58','123456'),(2,'20210002','小红','F',1,'13900000002','xh@example.com','NORMAL','2025-12-08 23:09:39','2025-12-23 21:00:58','123456');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_checkin`
--

DROP TABLE IF EXISTS `student_checkin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_checkin` (
  `checkin_id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `checkin_time` datetime NOT NULL,
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`checkin_id`),
  KEY `fk_checkin_session` (`session_id`),
  KEY `fk_checkin_student` (`student_id`),
  CONSTRAINT `fk_checkin_session` FOREIGN KEY (`session_id`) REFERENCES `attendance_session` (`session_id`),
  CONSTRAINT `fk_checkin_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_checkin`
--

LOCK TABLES `student_checkin` WRITE;
/*!40000 ALTER TABLE `student_checkin` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_checkin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `term`
--

DROP TABLE IF EXISTS `term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `term` (
  `term_id` bigint NOT NULL AUTO_INCREMENT,
  `term_code` varchar(32) NOT NULL,
  `term_name` varchar(64) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `is_current` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`term_id`),
  UNIQUE KEY `term_code` (`term_code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `term`
--

LOCK TABLES `term` WRITE;
/*!40000 ALTER TABLE `term` DISABLE KEYS */;
INSERT INTO `term` VALUES (1,'2024-2025-1','2024-2025学年第一学期','2024-09-01','2025-01-15',1,'2025-12-08 23:09:39','2025-12-08 23:09:39');
/*!40000 ALTER TABLE `term` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-24  9:04:15
