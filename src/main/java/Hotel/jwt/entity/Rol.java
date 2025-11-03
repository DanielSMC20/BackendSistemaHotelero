package Hotel.jwt.entity;

public enum Rol {
    ADMIN,        // Control total del sistema
    RECEPCIONISTA,    // Gestión de reservas, check-in, check-out, pagos
    CONTABILIDAD, // Consulta de reportes, facturación, cierres
    LIMPIEZA,     // Puede ver habitaciones en mantenimiento y estado
    GERENCIA      // Acceso a dashboard, estadísticas, reportes generales
}
