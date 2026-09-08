package com.hospital.hms.dto.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalAnalyticsDTO {

    private Double totalRevenue = 0.0;
    private Double monthlyRevenue = 0.0;
    private Long totalAppointments = 0L;
    private Long completedAppointments = 0L;
    private Long totalPatients = 0L;
    private Long totalDoctors = 0L;
    private Long totalPrescriptions = 0L;
    private Long totalLabOrders = 0L;

    // Inpatient / Bed Metrics
    private Long totalBeds = 0L;
    private Long occupiedBeds = 0L;
    private Long availableBeds = 0L;
    private Double bedOccupancyRate = 0.0;

    // Pharmacy Metrics
    private Long totalMedicines = 0L;
    private Long lowStockMedicines = 0L;

    // Charts & breakdowns
    private Map<String, Long> appointmentsByDepartment = new HashMap<>();
    private Map<String, Long> appointmentsByStatus = new HashMap<>();
    private List<MonthlyTrendDTO> monthlyTrends = new ArrayList<>();
    private List<DoctorWorkloadDTO> doctorWorkloads = new ArrayList<>();
    private List<DiagnosisStatDTO> topDiagnoses = new ArrayList<>();

    public HospitalAnalyticsDTO() {}

    public static class MonthlyTrendDTO {
        private String month;
        private Double revenue;
        private Long appointments;
        private Long patients;

        public MonthlyTrendDTO() {}

        public MonthlyTrendDTO(String month, Double revenue, Long appointments, Long patients) {
            this.month = month;
            this.revenue = revenue;
            this.appointments = appointments;
            this.patients = patients;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        public Long getAppointments() { return appointments; }
        public void setAppointments(Long appointments) { this.appointments = appointments; }
        public Long getPatients() { return patients; }
        public void setPatients(Long patients) { this.patients = patients; }
    }

    public static class DoctorWorkloadDTO {
        private String doctorName;
        private String department;
        private Long totalConsultations;
        private Long completedConsultations;
        private Double rating;

        public DoctorWorkloadDTO() {}

        public DoctorWorkloadDTO(String doctorName, String department, Long totalConsultations, Long completedConsultations, Double rating) {
            this.doctorName = doctorName;
            this.department = department;
            this.totalConsultations = totalConsultations;
            this.completedConsultations = completedConsultations;
            this.rating = rating;
        }

        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Long getTotalConsultations() { return totalConsultations; }
        public void setTotalConsultations(Long totalConsultations) { this.totalConsultations = totalConsultations; }
        public Long getCompletedConsultations() { return completedConsultations; }
        public void setCompletedConsultations(Long completedConsultations) { this.completedConsultations = completedConsultations; }
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
    }

    public static class DiagnosisStatDTO {
        private String diagnosis;
        private Long caseCount;
        private Double percentage;

        public DiagnosisStatDTO() {}

        public DiagnosisStatDTO(String diagnosis, Long caseCount, Double percentage) {
            this.diagnosis = diagnosis;
            this.caseCount = caseCount;
            this.percentage = percentage;
        }

        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
        public Long getCaseCount() { return caseCount; }
        public void setCaseCount(Long caseCount) { this.caseCount = caseCount; }
        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }
    }

    // Getters and Setters
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    public Double getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(Double monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    public Long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(Long totalAppointments) { this.totalAppointments = totalAppointments; }
    public Long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(Long completedAppointments) { this.completedAppointments = completedAppointments; }
    public Long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(Long totalPatients) { this.totalPatients = totalPatients; }
    public Long getTotalDoctors() { return totalDoctors; }
    public void setTotalDoctors(Long totalDoctors) { this.totalDoctors = totalDoctors; }
    public Long getTotalPrescriptions() { return totalPrescriptions; }
    public void setTotalPrescriptions(Long totalPrescriptions) { this.totalPrescriptions = totalPrescriptions; }
    public Long getTotalLabOrders() { return totalLabOrders; }
    public void setTotalLabOrders(Long totalLabOrders) { this.totalLabOrders = totalLabOrders; }
    public Long getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Long totalBeds) { this.totalBeds = totalBeds; }
    public Long getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(Long occupiedBeds) { this.occupiedBeds = occupiedBeds; }
    public Long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Long availableBeds) { this.availableBeds = availableBeds; }
    public Double getBedOccupancyRate() { return bedOccupancyRate; }
    public void setBedOccupancyRate(Double bedOccupancyRate) { this.bedOccupancyRate = bedOccupancyRate; }
    public Long getTotalMedicines() { return totalMedicines; }
    public void setTotalMedicines(Long totalMedicines) { this.totalMedicines = totalMedicines; }
    public Long getLowStockMedicines() { return lowStockMedicines; }
    public void setLowStockMedicines(Long lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }
    public Map<String, Long> getAppointmentsByDepartment() { return appointmentsByDepartment; }
    public void setAppointmentsByDepartment(Map<String, Long> appointmentsByDepartment) { this.appointmentsByDepartment = appointmentsByDepartment; }
    public Map<String, Long> getAppointmentsByStatus() { return appointmentsByStatus; }
    public void setAppointmentsByStatus(Map<String, Long> appointmentsByStatus) { this.appointmentsByStatus = appointmentsByStatus; }
    public List<MonthlyTrendDTO> getMonthlyTrends() { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrendDTO> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
    public List<DoctorWorkloadDTO> getDoctorWorkloads() { return doctorWorkloads; }
    public void setDoctorWorkloads(List<DoctorWorkloadDTO> doctorWorkloads) { this.doctorWorkloads = doctorWorkloads; }
    public List<DiagnosisStatDTO> getTopDiagnoses() { return topDiagnoses; }
    public void setTopDiagnoses(List<DiagnosisStatDTO> topDiagnoses) { this.topDiagnoses = topDiagnoses; }
}
