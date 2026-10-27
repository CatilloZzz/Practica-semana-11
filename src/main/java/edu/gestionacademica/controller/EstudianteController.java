package edu.gestionacademica.controller;

import edu.gestionacademica.model.Estudiante;
import edu.gestionacademica.repository.EstudianteRepository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EstudianteController {

    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbCarrera;

    @FXML private Button btnGuardar;
    @FXML private Button btnConsultar;
    @FXML private ProgressIndicator indicador;

    @FXML private TableView<Estudiante> tablaEstudiantes;
    @FXML private TableColumn<Estudiante, Object> colId;
    @FXML private TableColumn<Estudiante, String> colNombres;
    @FXML private TableColumn<Estudiante, String> colApellidos;
    @FXML private TableColumn<Estudiante, String> colCorreo;
    @FXML private TableColumn<Estudiante, String> colCarrera;

    @FXML private Label lblTotal;
    @FXML private Label lblEstado;

    private boolean ocupado;

    @FXML
    private void initialize() {
        cmbCarrera.getItems().setAll(
                "Ingeniería en Sistemas",
                "Administración de Empresas",
                "Contabilidad",
                "Derecho",
                "Marketing"
        );

        colId.setCellValueFactory(dato ->
                new ReadOnlyObjectWrapper<Object>(
                        dato.getValue().getId()
                ));

        colNombres.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        dato.getValue().getNombres()
                ));

        colApellidos.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        dato.getValue().getApellidos()
                ));

        colCorreo.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        dato.getValue().getCorreo()
                ));

        colCarrera.setCellValueFactory(dato ->
                new ReadOnlyStringWrapper(
                        dato.getValue().getCarrera()
                ));
    }

    @FXML
    private void guardarEstudiante() {
        if (ocupado || !validarFormulario()) {
            return;
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombres(txtNombres.getText().strip());
        estudiante.setApellidos(txtApellidos.getText().strip());
        estudiante.setCorreo(
                txtCorreo.getText().strip().toLowerCase(Locale.ROOT)
        );
        estudiante.setCarrera(cmbCarrera.getValue());

        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() throws Exception {
                EstudianteRepository repository =
                        new EstudianteRepository();

                repository.guardar(estudiante);
                return null;
            }
        };

        ejecutarTarea(
                tarea,
                "Guardando estudiante...",
                "No se pudo guardar el estudiante",
                resultado -> {
                    limpiarFormulario();

                    mostrarAlerta(
                            Alert.AlertType.INFORMATION,
                            "Registro exitoso",
                            "El estudiante se guardó correctamente."
                    );

                    cargarEstudiantes(true);
                }
        );
    }

    @FXML
    private void consultarEstudiantes() {
        if (ocupado) {
            return;
        }

        cargarEstudiantes(false);
    }

    private void cargarEstudiantes(boolean despuesDeGuardar) {
        Task<List<Estudiante>> tarea = new Task<>() {
            @Override
            protected List<Estudiante> call() throws Exception {
                EstudianteRepository repository =
                        new EstudianteRepository();

                return repository.listar();
            }
        };

        String tituloError = despuesDeGuardar
                ? "El registro se guardó, pero no se pudo actualizar la tabla"
                : "No se pudieron consultar los estudiantes";

        ejecutarTarea(
                tarea,
                "Consultando estudiantes...",
                tituloError,
                estudiantes -> {
                    tablaEstudiantes.getItems().setAll(estudiantes);

                    int total = estudiantes.size();
                    lblTotal.setText(
                            total + (total == 1
                                    ? " estudiante"
                                    : " estudiantes")
                    );

                    tablaEstudiantes.setPlaceholder(
                            new Label("No hay estudiantes registrados.")
                    );

                    lblEstado.setText(
                            despuesDeGuardar
                                    ? "Estudiante guardado y tabla actualizada."
                                    : "Consulta completada correctamente."
                    );
                }
        );
    }

    private boolean validarFormulario() {
        if (txtNombres.getText().isBlank()) {
            return advertir(
                    "Ingrese los nombres del estudiante.",
                    txtNombres
            );
        }

        if (txtApellidos.getText().isBlank()) {
            return advertir(
                    "Ingrese los apellidos del estudiante.",
                    txtApellidos
            );
        }

        String correo = txtCorreo.getText().strip();

        if (correo.isEmpty()) {
            return advertir(
                    "Ingrese el correo electrónico.",
                    txtCorreo
            );
        }

        if (!PATRON_CORREO.matcher(correo).matches()) {
            return advertir(
                    "Ingrese un correo válido. Ejemplo: estudiante@correo.com",
                    txtCorreo
            );
        }

        if (cmbCarrera.getValue() == null
                || cmbCarrera.getValue().isBlank()) {
            return advertir(
                    "Seleccione una carrera.",
                    cmbCarrera
            );
        }

        return true;
    }

    private boolean advertir(String mensaje, Control control) {
        mostrarAlerta(
                Alert.AlertType.WARNING,
                "Revise los datos",
                mensaje
        );

        control.requestFocus();
        return false;
    }

    private <T> void ejecutarTarea(
            Task<T> tarea,
            String mensaje,
            String tituloError,
            Consumer<T> alCompletar
    ) {
        cambiarEstadoOcupado(true);
        lblEstado.setText(mensaje);

        tarea.setOnSucceeded(evento -> {
            cambiarEstadoOcupado(false);
            alCompletar.accept(tarea.getValue());
        });

        tarea.setOnFailed(evento -> {
            cambiarEstadoOcupado(false);
            lblEstado.setText(tituloError + ".");

            Throwable error = tarea.getException();

            if (error != null) {
                error.printStackTrace();
            }

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    tituloError,
                    obtenerMensajeError(error)
            );
        });

        tarea.setOnCancelled(evento -> {
            cambiarEstadoOcupado(false);
            lblEstado.setText("Operación cancelada.");
        });

        Thread hilo = new Thread(tarea, "operacion-estudiantes");
        hilo.setDaemon(true);
        hilo.start();
    }

    private String obtenerMensajeError(Throwable error) {
        Throwable actual = error;

        while (actual != null) {
            if (actual instanceof IllegalArgumentException
                    && actual.getMessage() != null
                    && !actual.getMessage().isBlank()) {
                return actual.getMessage();
            }

            actual = actual.getCause();
        }

        return "No se pudo completar la operación. "
                + "Revise la conexión y la configuración de PostgreSQL. "
                + "El detalle técnico aparece en la consola.";
    }

    private void cambiarEstadoOcupado(boolean valor) {
        ocupado = valor;

        btnGuardar.setDisable(valor);
        btnConsultar.setDisable(valor);

        txtNombres.setDisable(valor);
        txtApellidos.setDisable(valor);
        txtCorreo.setDisable(valor);
        cmbCarrera.setDisable(valor);

        indicador.setVisible(valor);
        indicador.setManaged(valor);
    }

    private void limpiarFormulario() {
        txtNombres.clear();
        txtApellidos.clear();
        txtCorreo.clear();
        cmbCarrera.getSelectionModel().clearSelection();
        txtNombres.requestFocus();
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensaje
    ) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        if (tablaEstudiantes.getScene() != null) {
            alerta.initOwner(
                    tablaEstudiantes.getScene().getWindow()
            );
        }

        alerta.showAndWait();
    }

    public boolean estaOcupado() {
        return ocupado;
    }
}