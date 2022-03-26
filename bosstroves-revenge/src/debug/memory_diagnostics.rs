use bevy::app::App;
use bevy::core::FixedTimestep;
use bevy::diagnostic::{Diagnostic, DiagnosticId, Diagnostics};
use bevy::prelude::{ParallelSystemDescriptorCoercion, Plugin, ResMut};

use crate::util::uuid;

/// Adds a "physical" and a "virtual" memory diagnostic to an App.
pub struct MemoryDiagnosticsPlugin;

impl Plugin for MemoryDiagnosticsPlugin {
    fn build(&self, app: &mut App) {
        app.add_startup_system(Self::setup)
            .add_system(Self::update_diagnostics.with_run_criteria(FixedTimestep::step(0.1)));
    }
}

impl MemoryDiagnosticsPlugin {
    /// The physical memory taken up by the current process.
    /// Corresponds to "Resident Set Size" on POSIX and "Working Set Size" on Windows.
    pub const PHYSICAL_MEM: DiagnosticId = DiagnosticId(uuid!("862b2827-5d13-4ed0-a463-6770a986f10b"));

    /// The virtual memory taken up by the current process.
    /// Corresponds to "Virtual Size" on POSIX and "Pagefile Usage" on Windows.
    pub const VIRTUAL_MEM: DiagnosticId = DiagnosticId(uuid!("576a83d4-49bb-41e6-ae9c-5d8e26917cb9"));

    fn setup(mut diagnostics: ResMut<Diagnostics>) {
        diagnostics.add(Diagnostic::new(Self::PHYSICAL_MEM, "physical memory", 20));
        diagnostics.add(Diagnostic::new(Self::VIRTUAL_MEM, "virtual memory", 20));
    }

    fn update_diagnostics(mut diagnostics: ResMut<Diagnostics>) {
        if let Some(stats) = memory_stats::memory_stats() {
            diagnostics.add_measurement(Self::PHYSICAL_MEM, stats.physical_mem as f64);
            diagnostics.add_measurement(Self::VIRTUAL_MEM, stats.virtual_mem as f64);
        }
    }
}
