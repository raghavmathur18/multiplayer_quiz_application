package com.quizapp.gui.common;

import com.quizapp.models.Result;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.data.general.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Reusable analytics panel using JFreeChart.
 * Provides bar charts, line charts, and pie charts for quiz analytics.
 */
public class AnalyticsPanel extends JPanel {

    public AnalyticsPanel() {
        setBackground(UITheme.BG_CONTENT);
        setLayout(new BorderLayout());
    }

    /**
     * Creates a bar chart showing student scores in a quiz.
     */
    public static ChartPanel createStudentScoreBarChart(List<Result> results, String title) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Result r : results) {
            double pct = r.getTotalQuestions() > 0 ? (double) r.getScore() / r.getTotalQuestions() * 100 : 0;
            dataset.addValue(pct, "Score %", r.getStudentName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            title, "Student", "Score (%)", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );

        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UITheme.PRIMARY);
        renderer.setMaximumBarWidth(0.1);
        renderer.setShadowVisible(false);

        plot.getRangeAxis().setRange(0, 100);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UITheme.BG_CARD);
        return cp;
    }

    /**
     * Creates a line chart showing student performance trend over multiple quizzes.
     */
    public static ChartPanel createPerformanceTrendLineChart(List<Result> results, String studentName) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int quizNum = 1;
        for (Result r : results) {
            double pct = r.getTotalQuestions() > 0 ? (double) r.getScore() / r.getTotalQuestions() * 100 : 0;
            dataset.addValue(pct, studentName, "Quiz " + quizNum++);
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Performance Trend", "Quiz", "Score (%)", dataset,
            PlotOrientation.VERTICAL, true, true, false
        );

        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UITheme.SECONDARY);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);

        plot.getRangeAxis().setRange(0, 100);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UITheme.BG_CARD);
        return cp;
    }

    /**
     * Creates a pie chart showing correct vs incorrect answers.
     */
    public static ChartPanel createCorrectIncorrectPieChart(int correct, int incorrect, String title) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Correct (" + correct + ")", correct);
        dataset.setValue("Incorrect (" + incorrect + ")", incorrect);

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        styleChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Correct (" + correct + ")", UITheme.SECONDARY);
        plot.setSectionPaint("Incorrect (" + incorrect + ")", UITheme.DANGER);
        plot.setBackgroundPaint(UITheme.BG_CARD);
        plot.setOutlineVisible(false);
        plot.setLabelFont(UITheme.FONT_SMALL);
        plot.setLabelBackgroundPaint(UITheme.BG_CARD);
        plot.setLabelOutlinePaint(UITheme.BORDER);
        plot.setLabelShadowPaint(null);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UITheme.BG_CARD);
        return cp;
    }

    /**
     * Creates a bar chart showing scores per quiz for a student.
     */
    public static ChartPanel createStudentQuizScoresBarChart(List<Result> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Result r : results) {
            double pct = r.getTotalQuestions() > 0 ? (double) r.getScore() / r.getTotalQuestions() * 100 : 0;
            String label = r.getQuizTitle().length() > 15 ? r.getQuizTitle().substring(0, 12) + "..." : r.getQuizTitle();
            dataset.addValue(pct, "Score %", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "My Quiz Scores", "Quiz", "Score (%)", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );

        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UITheme.STUDENT_ACCENT);
        renderer.setMaximumBarWidth(0.12);
        renderer.setShadowVisible(false);
        plot.getRangeAxis().setRange(0, 100);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UITheme.BG_CARD);
        return cp;
    }

    /**
     * Creates a multi-student performance comparison bar chart (for teacher).
     */
    public static ChartPanel createClassPerformanceBarChart(List<Result> results, String quizTitle) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Group by student, use latest attempt
        Map<String, Double> studentScores = new LinkedHashMap<>();
        for (Result r : results) {
            double pct = r.getTotalQuestions() > 0 ? (double) r.getScore() / r.getTotalQuestions() * 100 : 0;
            studentScores.put(r.getStudentName(), pct);
        }
        for (Map.Entry<String, Double> e : studentScores.entrySet()) {
            dataset.addValue(e.getValue(), "Score %", e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Class Performance — " + quizTitle, "Student", "Score (%)", dataset,
            PlotOrientation.VERTICAL, false, true, false
        );

        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, UITheme.TEACHER_ACCENT);
        renderer.setMaximumBarWidth(0.1);
        renderer.setShadowVisible(false);
        plot.getRangeAxis().setRange(0, 100);
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(UITheme.BG_CARD);
        return cp;
    }

    private static void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(UITheme.BG_CARD);
        chart.getTitle().setPaint(UITheme.TEXT_PRIMARY);
        chart.getTitle().setFont(UITheme.FONT_SUBHEADING);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(UITheme.BG_CARD);
            chart.getLegend().setItemPaint(UITheme.TEXT_SECONDARY);
        }

        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(UITheme.BG_CARD);
        plot.setOutlinePaint(UITheme.BORDER);

        if (plot instanceof CategoryPlot cp) {
            cp.setRangeGridlinePaint(UITheme.BORDER);
            cp.setDomainGridlinePaint(UITheme.BORDER);
            cp.getDomainAxis().setTickLabelPaint(UITheme.TEXT_SECONDARY);
            cp.getDomainAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            cp.getRangeAxis().setTickLabelPaint(UITheme.TEXT_SECONDARY);
            cp.getRangeAxis().setLabelPaint(UITheme.TEXT_SECONDARY);
            cp.getDomainAxis().setAxisLinePaint(UITheme.BORDER);
            cp.getRangeAxis().setAxisLinePaint(UITheme.BORDER);
        }
    }
}
