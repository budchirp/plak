use core::str;
use std::{
    process::{Child, Command},
    thread,
    time::Duration,
};

pub struct CommandLine {
    child: Child,
}

pub enum ProcessStatus {
    Running,
    Done,
    Error(String),
}

impl CommandLine {
    pub fn new(child: Child) -> Self {
        Self { child }
    }

    pub fn get_path(bin: &str) -> String {
        Command::new("which")
            .arg(bin)
            .output()
            .map(|output| {
                str::from_utf8(&output.stdout)
                    .unwrap_or(bin)
                    .trim()
                    .to_string()
            })
            .unwrap_or_else(|_| bin.to_string())
    }

    pub fn monitor<F>(mut self, mut callback: F)
    where
        F: FnMut(ProcessStatus) + 'static,
    {
        match self.child.try_wait() {
            Ok(Some(_)) => {
                callback(ProcessStatus::Done);

                println!("nigger");

                thread::sleep(Duration::from_secs(1));
            }

            Ok(None) => {
                callback(ProcessStatus::Running);

                thread::sleep(Duration::from_secs(1));
            }

            Err(error) => {
                callback(ProcessStatus::Error(error.to_string()));
            }
        }
    }
}
