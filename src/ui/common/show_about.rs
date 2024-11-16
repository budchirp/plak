use adw::{prelude::*, AboutDialog, Application};

use crate::constants;

pub struct ShowAboutDialog {}

impl ShowAboutDialog {
    pub fn new(app: &Application) {
        let about = AboutDialog::builder()
            .application_name(constants::APP_NAME)
            .application_icon(constants::APP_ID)
            .developer_name(constants::DEVELOPERS[0])
            .version(constants::VERSION)
            .developers(constants::DEVELOPERS)
            .copyright(format!("@ {}", constants::DEVELOPERS[0]))
            .build();

        about.present(Some(&app.active_window().unwrap()));
    }
}
