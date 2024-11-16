use adw::{prelude::*, Application, ApplicationWindow};

use crate::ui::main_window::MainWindow;

pub struct Window {
    window: ApplicationWindow,
}

impl Window {
    pub fn new(app: &Application) -> Self {
        let mut main_window = MainWindow::new(app);
        main_window.build_ui();

        Self {
            window: main_window.window,
        }
    }

    pub fn run(&self) {
        self.window.present();
    }
}
