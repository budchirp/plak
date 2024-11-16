use adw::{prelude::*, Application};
use gtk::gio::{self};

use crate::{constants, ui::common::show_about::ShowAboutDialog, window::Window};

pub struct App {
    app: Application,
}

impl App {
    pub fn new() -> Self {
        Self {
            app: Application::new(
                Some(constants::APP_ID),
                gtk::gio::ApplicationFlags::FLAGS_NONE,
            ),
        }
    }

    pub fn run(&self) {
        self.setup_actions();
        self.setup_accels();

        self.app.connect_activate(|app| {
            let window = Window::new(app);
            window.run();
        });

        self.app.run();
    }

    fn setup_accels(&self) {
        self.app.set_accels_for_action("app.quit", &["<primary>q"]);
        self.app.set_accels_for_action("app.about", &["<primary>a"]);
    }

    fn setup_actions(&self) {
        let app_clone = self.app.clone();
        let quit_action = gio::ActionEntry::builder("quit")
            .activate(move |_, _, _| app_clone.quit())
            .build();

        let app_clone = self.app.clone();
        let about_action = gio::ActionEntry::builder("about")
            .activate(move |_, _, _| ShowAboutDialog::new(&app_clone))
            .build();

        self.app.add_action_entries([quit_action, about_action]);
    }
}
