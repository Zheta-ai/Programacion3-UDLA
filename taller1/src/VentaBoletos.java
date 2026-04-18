import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class VentaBoletos extends JFrame {

    // Colas para controlar los 20 asientos disponibles de cada furgoneta.
    private Queue<String> colaQuitoGuayaquil = new LinkedList<>();
    private Queue<String> colaQuitoCuenca = new LinkedList<>();
    private Queue<String> colaQuitoLoja = new LinkedList<>();

    // Pila para llevar el registro de las transacciones (historial de ventas).
    private Stack<Transaccion> historialVentas = new Stack<>();

    // Acumulador del dinero total.
    private double totalRecaudado = 0.0;

    // Componentes de la interfaz visual.
    private JComboBox<String> comboRutas;
    private JTextField txtCedula, txtNombre, txtCantidad;
    private JButton btnComprar;
    private JTextArea areaDetalle;
    private JLabel lblVendidos, lblDisponibles, lblRecaudado;

    public VentaBoletos() {
        setTitle("Sistema de Gestión - Transporte");
        setSize(450, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        inicializarAsientos();
        construirInterfaz();
        actualizarEstadisticas();
    }

    // Llenamos las colas con 20 "asientos" al iniciar el programa.
    private void inicializarAsientos() {
        for (int i = 0; i < 20; i++) {
            colaQuitoGuayaquil.add("Asiento");
            colaQuitoCuenca.add("Asiento");
            colaQuitoLoja.add("Asiento");
        }
    }

    // Armamos la ventana gráfica con todos sus elementos.
    private void construirInterfaz() {
        String[] rutas = {"QUITO - GUAYAQUIL", "QUITO - CUENCA", "QUITO - LOJA"};
        comboRutas = new JComboBox<>(rutas);

        txtCedula = new JTextField(15);
        txtNombre = new JTextField(15);
        txtCantidad = new JTextField(5);
        btnComprar = new JButton("Comprar");

        areaDetalle = new JTextArea(15, 35);
        areaDetalle.setEditable(false);
        JScrollPane scrollArea = new JScrollPane(areaDetalle);

        lblVendidos = new JLabel("Vendidos (Ruta actual): 0");
        lblDisponibles = new JLabel("Disponibles (Ruta actual): 20");
        lblRecaudado = new JLabel("Recaudación Total: $0.00");

        add(new JLabel("Seleccione la Ruta:"));
        add(comboRutas);
        add(new JLabel("Cédula:"));
        add(txtCedula);
        add(new JLabel("Nombre:"));
        add(txtNombre);
        add(new JLabel("Cantidad Boletos:"));
        add(txtCantidad);
        add(btnComprar);
        add(scrollArea);
        add(lblVendidos);
        add(lblDisponibles);
        add(lblRecaudado);

        btnComprar.addActionListener(e -> procesarCompra());
        comboRutas.addActionListener(e -> actualizarEstadisticas());
    }

    // Lógica principal de validación y registro al darle al botón Comprar.
    private void procesarCompra() {

        String rutaSel = (String) comboRutas.getSelectedItem();
        String cedula = txtCedula.getText().trim();
        String nombre = txtNombre.getText().trim();
        String cantStr = txtCantidad.getText().trim();

        // Validación básica de los inputs para evitar errores o campos nulos.
        if (cedula.isEmpty() || nombre.isEmpty() || cantStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Faltan datos. Llena todos los campos.");
            return;
        }

        // Verificamos que no ingresen letras en la cantidad.
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad de boletos debe ser un número entero.");
            return;
        }

        // Regla: no se pueden comprar más de 5 boletos de golpe.
        if (cantidad <= 0 || cantidad > 5) {
            JOptionPane.showMessageDialog(this, "Por regla, solo puedes comprar entre 1 y 5 boletos a la vez.");
            return;
        }

        // Recorremos la pila para contar cuántos boletos ya tiene esta cédula en el historial.
        int compradosPreviamente = 0;
        for (Transaccion t : historialVentas) {
            if (t.getCedula().equals(cedula)) {
                compradosPreviamente += t.getCantidadBoletos();
            }
        }

        // Validamos el límite estricto de 5 boletos acumulados por persona.
        if ((compradosPreviamente + cantidad) > 5) {
            JOptionPane.showMessageDialog(this, "Bloqueado: La cédula " + cedula +
                    " ya tiene " + compradosPreviamente + " boletos en el sistema.");
            return;
        }

        Queue<String> colaSeleccionada = null;
        double precioBoleto = 0.0;

        // Apuntamos a la estructura de la ruta elegida y definimos su tarifa.
        if (rutaSel.contains("GUAYAQUIL")) {
            colaSeleccionada = colaQuitoGuayaquil;
            precioBoleto = 10.50;
        } else if (rutaSel.contains("CUENCA")) {
            colaSeleccionada = colaQuitoCuenca;
            precioBoleto = 12.75;
        } else if (rutaSel.contains("LOJA")) {
            colaSeleccionada = colaQuitoLoja;
            precioBoleto = 15.00;
        }

        // Verificamos que la furgoneta tenga cupos suficientes en la cola.
        if (colaSeleccionada.size() < cantidad) {
            JOptionPane.showMessageDialog(this, "Cupo insuficiente. Solo quedan " + colaSeleccionada.size() + " asientos.");
            return;
        }

        // Despachamos los asientos sacándolos de la cola (poll).
        for (int i = 0; i < cantidad; i++) {
            colaSeleccionada.poll();
        }

        // Sumamos el dinero al total global.
        double subtotal = precioBoleto * cantidad;
        totalRecaudado += subtotal;

        // Metemos el registro de esta venta en la pila (push).
        Transaccion nuevaVenta = new Transaccion(cedula, rutaSel, cantidad);
        historialVentas.push(nuevaVenta);

        // Imprimimos el comprobante en la consola visual.
        String detalle = "Ruta: " + rutaSel + " | Cédula: " + cedula + " | Pasajero: " + nombre + " | Boletos: " + cantidad + " | Total: $" + subtotal + "\n";
        areaDetalle.append(detalle);

        // Dejamos los campos vacíos para la próxima transacción.
        txtCedula.setText("");
        txtNombre.setText("");
        txtCantidad.setText("");

        // Actualizamos los labels con la información nueva.
        actualizarEstadisticas();

        JOptionPane.showMessageDialog(this, "Venta ingresada correctamente al sistema.");
    }

    // Calcula cuántos vendidos/disponibles hay revisando el size() de la cola.
    private void actualizarEstadisticas() {
        int indexRuta = comboRutas.getSelectedIndex();
        int disponibles = 0;

        if (indexRuta == 0) disponibles = colaQuitoGuayaquil.size();
        else if (indexRuta == 1) disponibles = colaQuitoCuenca.size();
        else if (indexRuta == 2) disponibles = colaQuitoLoja.size();

        int vendidos = 20 - disponibles;

        lblVendidos.setText("Vendidos (Ruta actual): " + vendidos);
        lblDisponibles.setText("Disponibles (Ruta actual): " + disponibles);
        lblRecaudado.setText(String.format("Recaudación Total: $%.2f", totalRecaudado));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentaBoletos().setVisible(true);
        });
    }
}