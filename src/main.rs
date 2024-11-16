use app::App;
use init::Init;

mod app;
mod config;
mod constants;
mod init;
mod minecraft;
mod network;
mod toml;
mod ui;
mod window;

fn main() {
    let init = Init::new();
    let _ = init.initialize();

    let app = App::new();
    app.run();
}
