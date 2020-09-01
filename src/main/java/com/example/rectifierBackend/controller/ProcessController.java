package com.example.rectifierBackend.controller;

import com.example.rectifierBackend.message.request.ProcessFilter;
import com.example.rectifierBackend.model.Bath;
import com.example.rectifierBackend.model.Process;
import com.example.rectifierBackend.model.Sample;
import com.example.rectifierBackend.model.User;
import com.example.rectifierBackend.repository.BathRepository;
import com.example.rectifierBackend.repository.ProcessRepository;
import com.example.rectifierBackend.repository.SampleRepository;
import com.example.rectifierBackend.service.RectifierService;
import com.example.rectifierBackend.service.event.Event;
import com.example.rectifierBackend.service.event.EventService;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.*;
import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.Marker;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/process")
@RestController
@CrossOrigin
public class ProcessController {

    private final ProcessRepository processRepository;
    private final BathRepository bathRepository;
    private final SampleRepository sampleRepository;
    private final RectifierService rectifierService;
    private final EventService eventService;
    private final Log logger = LogFactory.getLog(getClass());

    public ProcessController(ProcessRepository processRepository, BathRepository bathRepository,
                             SampleRepository sampleRepository, RectifierService rectifierService,
                             EventService eventService) {
        this.processRepository = processRepository;
        this.bathRepository = bathRepository;
        this.sampleRepository = sampleRepository;
        this.rectifierService = rectifierService;
        this.eventService = eventService;
    }

    @GetMapping("{processId}")
    ResponseEntity<?> getOne(@PathVariable long processId) {
        Process process =
                processRepository.findById(processId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found."));
        return ResponseEntity.ok(process);
    }

    @GetMapping("{processId}/samples")
    ResponseEntity<?> getSamples(@PathVariable long processId) {
        return ResponseEntity.ok(sampleRepository.findAllByProcessIdOrderByTimestampAsc(processId));
    }

    @DeleteMapping("{processId}")
    ResponseEntity<?> delete(@PathVariable long processId) {
        processRepository.deleteById(processId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("")
    ResponseEntity<?> add(@RequestBody Process process) {
        processRepository.save(process);
        return ResponseEntity.ok(process);
    }

    @GetMapping("")
    ResponseEntity<?> getAll() {
        return ResponseEntity.ok(processRepository.findAll());
    }

    @PostMapping("filter")
    ResponseEntity<?> postFilter(@Valid @RequestBody ProcessFilter filter) {
        return ResponseEntity.ok(processRepository.findByInsertCodeIgnoreCaseContainingAndElementNameIgnoreCaseContainingAndDrawingNumberIgnoreCaseContainingAndOrderNumberIgnoreCaseContainingAndMonterIgnoreCaseContainingAndStopTimestampGreaterThanAndStartTimestampLessThan(filter.getInsertCode(), filter.getElementName(), filter.getDrawingNumber(), filter.getOrderNumber(), filter.getMonter(), filter.getTimeFrom(), filter.getTimeTo()));
    }

    @PostMapping("/start")
    ResponseEntity<?> startProcess(@Valid @RequestBody Process process) {
        User user = User.getCurrentUser().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Bath bath =
                bathRepository.findById(process.getBathId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bath not found"));
//        if(bath.getUser() == null || bath.getUser().getId() != user.getId()) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bath not occupied by current user");
//        }
        if (bath.getProcess() != null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A process is already started for this bath");
        }

        bath.setProcess(process);
        process.setOperator(user);
        processRepository.save(process);
        bathRepository.save(bath);
        rectifierService.startProcess(process.getId());
        eventService.dispatchEvent(new com.example.rectifierBackend.service.event.Event<>(Event.PROCESS_STARTED,
                process));
        return ResponseEntity.ok(process);
    }

    @PostMapping("/{processId}/stop")
    ResponseEntity<?> stopProcess(@PathVariable long processId) {
        User user = User.getCurrentUser().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Process process =
                processRepository.findById(processId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found."));
        Bath bath =
                bathRepository.findById(process.getBathId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bath not found."));
        if (process.getOperator() == null || process.getOperator().getId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Process not started by current user");
        }
        rectifierService.stopProcess(processId);
        bath.setProcess(null);
        bathRepository.save(bath);
        eventService.dispatchEvent(new Event<>(Event.PROCESS_STOPPED, process));
        return ResponseEntity.noContent().build();
    }

    private Image createChart(List<?> xData, List<? extends Number> yData, String title, String unit) throws IOException {
        XYChart chart = new XYChart(750, 300);
        chart.setTitle(title);
        chart.setXAxisTitle("Czas");
        chart.setYAxisTitle(title + " [" + unit + "]");
        chart.getStyler().setLegendVisible(false);
        XYSeries series = chart.addSeries(title, xData, yData);
        series.setMarker(new Marker() {
            @Override
            public void paint(Graphics2D graphics2D, double v, double v1, int i) {

            }
        });

        series.setLabel("label");

        double scaleFactor = 3;
        float finalScaleFactor = 0.7f;

        BufferedImage bufferedImage = new BufferedImage((int) (chart.getWidth() * scaleFactor),
                (int) (chart.getHeight() * scaleFactor), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();

        AffineTransform at = graphics2D.getTransform();
        at.scale(scaleFactor, scaleFactor);
        graphics2D.setTransform(at);
        chart.paint(graphics2D, chart.getWidth(), chart.getHeight());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageIO.write(bufferedImage, "png", baos);

        Image image = Image.getInstance(baos.toByteArray());
        image.scaleAbsolute(chart.getWidth() * finalScaleFactor, (float) chart.getHeight() * finalScaleFactor);
        return image;
    }

    private Cell textToCell(String text, HorizontalAlignment horizontalAlignment) {
        Paragraph paragraph = new Paragraph(text);
        paragraph.getFont().setSize(8);
        Cell cell = new Cell(paragraph);
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setVerticalAlignment(VerticalAlignment.TOP);
        cell.setBorder(Cell.NO_BORDER);
        return cell;
    }

    @GetMapping(value = "{processId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<StreamingResponseBody> testReport(@PathVariable long processId) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Process process =
                processRepository.findById(processId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found."));
        Bath bath =
                bathRepository.findById(process.getBathId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bath not found."));
        StreamingResponseBody responseBody = (OutputStream outputStream) -> {
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            HeaderFooter header = new HeaderFooter(new Phrase("Technologie Galwaniczne - Raport", new Font(bf)), false);
            header.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.setHeader(header);
            document.open();
            Table table = new Table(6);
            table.setWidth(90);
            table.setHorizontalAlignment(HorizontalAlignment.JUSTIFIED);
            table.setPadding(2);
            table.addCell(textToCell("Proces: " + processId, HorizontalAlignment.CENTER));
            table.addCell(textToCell("Stanowisko: " + bath.getName(), HorizontalAlignment.CENTER));
            Cell startCell = textToCell(dateFormat.format(process.getStartTimestamp()), HorizontalAlignment.CENTER);
            startCell.setColspan(2);
            table.addCell(startCell);
            Cell stopCell = textToCell(dateFormat.format(process.getStopTimestamp()), HorizontalAlignment.CENTER);
            stopCell.setColspan(2);
            table.addCell(stopCell);
            Cell separatorCell = new Cell();
            separatorCell.setColspan(6);
            table.addCell(separatorCell);
            table.addCell(textToCell("Kod wkladu:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getInsertCode() + "", HorizontalAlignment.LEFT));
            table.addCell(textToCell("Nazwa elementu:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getElementName(), HorizontalAlignment.LEFT));
            table.addCell(textToCell("Typ chromu:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getChromeType(), HorizontalAlignment.LEFT));
            table.addCell(textToCell("Numer rysunku:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getDrawingNumber(), HorizontalAlignment.LEFT));
            table.addCell(textToCell("Numer zlecenia:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getOrderNumber(), HorizontalAlignment.LEFT));
            table.addCell(textToCell("Operacja:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getOperation(), HorizontalAlignment.LEFT));
            table.addCell(textToCell("Monter:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getMonter(), HorizontalAlignment.LEFT));
            table.addCell(textToCell("Operator:", HorizontalAlignment.RIGHT));
            table.addCell(textToCell(process.getOperator().getUsername(), HorizontalAlignment.LEFT));
            document.add(table);

            List<Sample> samples = sampleRepository.findAllByProcessIdOrderByTimestampAsc(process.getId());
            List<Double> voltages = new ArrayList<>(samples.size());
            List<Double> currents = new ArrayList<>(samples.size());
            List<Double> temperatures = new ArrayList<>(samples.size());
            List<Timestamp> timestamps = new ArrayList<>(samples.size());
            for (Sample sample : samples) {
                voltages.add(sample.getVoltage());
                currents.add(sample.getCurrent());
                temperatures.add(sample.getTemperature());
                timestamps.add(sample.getTimestamp());
            }

            Image voltageChart = createChart(timestamps, voltages, "Napięcie", "V");
            Image currentChart = createChart(timestamps, currents, "Prąd", "A");
            Image temperatureChart = createChart(timestamps, temperatures, "Temperatura", "°C");

            voltageChart.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            currentChart.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            temperatureChart.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);

            Paragraph graphs = new Paragraph();
            graphs.add(voltageChart);
            graphs.add(currentChart);
            graphs.add(temperatureChart);
            document.add(graphs);
            document.close();
        };
        return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE).header(HttpHeaders.CONTENT_DISPOSITION, "inline;")// filename=\"Raport" + process.getId() + ".pdf\"")
                .body(responseBody);
    }
}
