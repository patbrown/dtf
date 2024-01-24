(setq inhibit-startup-message t)
(setq auto-save-list-file-prefix "~/.emacs.d/autosave/")
(setq auto-save-file-name-transforms
      '((".*" "~/.emacs.d/autosave/" t)))

;; BASE SYSTEM
(setq package-enable-at-startup nil)
(defvar bootstrap-version)
(let ((bootstrap-file
       (expand-file-name
        "straight/repos/straight.el/bootstrap.el"
        (or (bound-and-true-p straight-base-dir)
            user-emacs-directory)))
      (bootstrap-version 7))
  (unless (file-exists-p bootstrap-file)
    (with-current-buffer
        (url-retrieve-synchronously
         "https://raw.githubusercontent.com/radian-software/straight.el/develop/install.el"
         'silent 'inhibit-cookies)
      (goto-char (point-max))
      (eval-print-last-sexp)))
  (load bootstrap-file nil 'nomessage))

(straight-use-package 'use-package)
(use-package straight :custom (straight-use-package-by-default))

(load-file "~/.emacs.d/lisp/kbd.el")
(load-file "~/.emacs.d/lisp/registers.el")

;;; GERNERAL
(defalias 'yes-or-no-p 'y-or-n-p)
(setq-default window-combination-resize t x-stretch-cursor t)
(unless (display-graphic-p) (xterm-mouse-mode 1))
(setq backup-directory-alist '(("" . "~/.emacs.d/emacs-backup")))
(setq dired-dwim-target t)
(setq undo-limit 80000000 truncate-string-ellipsis "…" scroll-margin 2)
(display-time-mode 1)
(global-subword-mode 1)
(setq blink-cursor-interval 0.4)
(blink-cursor-mode)
(column-number-mode)
(set-frame-parameter (selected-frame) 'fullscreen 'maximized)
(add-to-list 'default-frame-alist '(fullscreen . maximized))
(put 'downcase-region 'disabled nil)
(put 'narrow-to-region 'disabled nil)
(put 'narrow-to-page 'disabled nil)
(put 'narrow-to-defun 'disabled nil)
(straight-use-package 'project)
(setq make-backup-files nil)

;; Buffer Navigation
(setq ns-pop-up-frames nil)
(size-indication-mode t)
(transient-mark-mode 1)
(setq scroll-margin 0
      scroll-conservatively 100000
      scroll-preserve-screen-position 1)
(fset 'yes-or-no-p 'y-or-n-p)
(setq vc-follow-symlinks t)

;; General Look and Feel
(menu-bar-mode -1)
(setq dired-use-ls-dired nil)
(setq display-time-24hr-format t)
(setq display-time-load-average t)
(display-time)
(if (fboundp 'fringe-mode)
    (fringe-mode -1))
(display-line-numbers-mode 1)
(setq linum-format "%d ")
(global-display-line-numbers-mode t)
(setq frame-title-format "%b")
(show-paren-mode 1)
(global-hl-line-mode 1)
(set-face-background hl-line-face "snow")
(set-face-underline hl-line-face t)
;; disable CJK coding/encoding (Chinese/Japanese/Korean characters)
(setq utf-translate-cjk-mode nil)

(set-language-environment 'utf-8)
(setq locale-coding-system 'utf-8)

;; set the default encoding system
(prefer-coding-system 'utf-8)
(setq default-file-name-coding-system 'utf-8)
(set-default-coding-systems 'utf-8)
(set-terminal-coding-system 'utf-8)
(set-keyboard-coding-system 'utf-8)
;; backwards compatibility as default-buffer-file-coding-system
;; is deprecated in 23.2.
(if (boundp buffer-file-coding-system)
    (setq buffer-file-coding-system 'utf-8)
  (setq default-buffer-file-coding-system 'utf-8))
;; Treat clipboard input as UTF-8 string first; compound text next, etc.
(setq x-select-request-type '(UTF8_STRING COMPOUND_TEXT TEXT STRING))

(defun smarter-move-beginning-of-line (arg)
  (interactive "^p")
  (setq arg (or arg 1))

  ;; Move lines first
  (when (/= arg 1)
    (let ((line-move-visual nil))
      (forward-line (1- arg))))

  (let ((orig-point (point)))
    (back-to-indentation)
    (when (= orig-point (point))
      (move-beginning-of-line 1))))

(use-package ace-jump-zap :straight t :defer t :init (require 'ace-jump-zap))
(use-package aggressive-indent :straight t :ensure t)
(use-package bufler :straight t :ensure t)
(use-package burly :straight t :defer t)
(use-package counsel-projectile :straight t :after projectile :config (counsel-projectile-mode 1))
(use-package discover :straight t :defer t :config (global-discover-mode 1))
(use-package discover-my-major :straight t :defer t :config (global-set-key (kbd "C-h C-m") 'discover-my-major))
(use-package lsp-ivy :straight t)
(use-package lsp-ui :straight t :commands lsp-ui-mode)
(use-package magit :straight t)
(use-package markdown-mode :straight t :defer t)
(use-package multiple-cursors :straight t :defer t)
(use-package paredit :straight t :ensure t
  :init
  (eval-after-load "paredit"
  #'(define-key paredit-mode-map (kbd "C-j") 'newline-and-indent))
  (autoload 'enable-paredit-mode "paredit" "Turn on pseudo-structural editing of Lisp code." t)
  (add-hook 'emacs-lisp-mode-hook #'enable-paredit-mode)
  (add-hook 'prog-mode-hook #'enable-paredit-mode)
  (add-hook 'eval-expression-minibuffer-setup-hook #'enable-paredit-mode)
  (add-hook 'ielm-mode-hook #'enable-paredit-mode)
  (add-hook 'lisp-mode-hook #'enable-paredit-mode)
  (add-hook 'lisp-interaction-mode-hook #'enable-paredit-mode)
  (add-hook 'scheme-mode-hook #'enable-paredit-mode)
  (add-hook 'clojure-mode-hook #'enable-paredit-mode)
  (add-hook 'cider-repl-mode-hook #'enable-paredit-mode)
  :config
  (show-paren-mode t)
  (define-key paredit-mode-map (kbd "M-s") nil)
  :diminish nil)

(use-package paredit-everywhere :straight t
  :config
  (add-hook 'prog-mode-hook 'paredit-everywhere-mode)
  (add-hook 'css-mode-hook 'paredit-everywhere-mode))

(use-package parseedn :straight t)
(use-package project :straight t :defer t)

(use-package projectile :straight t :diminish projectile-mode
  :config (projectile-mode)
  :init
  (when (file-directory-p "~/")
    (setq projectile-project-search-path '("~/")))
  (setq projectile-switch-project-action #'projectile-dired))

(use-package rainbow-delimiters :straight t
  :init
    (progn (add-hook 'cider-repl-mode-hook #'rainbow-delimiters-mode)
	 (add-hook 'clojure-mode-hook #'rainbow-delimiters-mode)
         (add-hook 'cider-mode-hook #'rainbow-delimiters-mode)
         (add-hook 'prog-mode-hook #'rainbow-delimiters-mode)))

(use-package vimish-fold :straight t :init (vimish-fold-global-mode 1))

(use-package which-key :straight t :defer t :init (require 'which-key) (which-key-mode))

(use-package yaml-mode :straight t :config (add-hook 'yaml-mode-hook (lambda () (define-key yaml-mode-map "\C-m" 'newline-and-indent))))

(use-package yasnippet :straight t :init (progn (require 'yasnippet) (yas-global-mode 1)))

(use-package lsp-mode :straight t :ensure t
  :hook ((clojure-mode . lsp))
  :config (lsp-enable-which-key-integration t))

(use-package ivy-rich
  :straight t
  :after counsel
  :init (setq ivy-rich-path-style 'abbrev
              ivy-virtual-abbreviate 'full)
  :config (ivy-mode) (ivy-rich-mode))

(use-package counsel :straight t :after ivy :config (counsel-mode))

(use-package swiper :straight t :after ivy :bind (("C-s" . swiper)))

(use-package cider :straight t :ensure t)

(use-package ace-jump-mode
  :straight t
  :init
  (autoload 'ace-jump-mode "ace-jump-mode" "Emacs quick move minor mode" t)
  ;(define-key global-map (kbd "C-c SPC") 'ace-jump-mode)
  (setq ace-jump-mode-gray-background nil)
  (setq ace-jump-mode-move-keys
        '(?j ?f ?k ?d ?l ?s ?\; ?a ?h ?g ?u ?r ?i ?e ?o ?w ?p ?q ?y ?t ?m ?v ?, ?c ?. ?x ?/ ?z ?n ?b ?7 ?4 ?8 ?3 ?9 ?2 ?0 ?1 ?6 ?5)))

(use-package windmove
  :straight t
  :config
  (defun ignore-error-wrapper (fn)
    "Funtion return new function that ignore errors.
   The function wraps a function with `ignore-errors' macro."
    (lexical-let ((fn fn))
      (lambda ()
        (interactive)
        (ignore-errors
          (funcall fn))))))

(use-package spacemacs-theme :straight t :ensure t :init (load-theme 'spacemacs-dark t))
(use-package transient :straight t)
(use-package company :straight t)

(use-package ivy :straight t :defer 0.1 :diminish 
  :bind (("C-c C-r" . ivy-resume))
  :custom
  (ivy-count-format "(%d/%d) ")
  (ivy-use-virtual-buffers t)
  :init
  (defvar my-ivy-builders '(ivy--regex-ignore-order
                            ivy--regex-fuzzy
			    ivy--regex-plus)
  "Preferred values for `ivy--regex-function'.")
(use-package undo-tree
  :straight t
  :defer t
  :ensure t
  :diminish undo-tree-mode
  :init
  (progn
    (require 'undo-tree)
    (global-undo-tree-mode)
    (setq undo-tree-visualizer-timestamps t)
    (setq undo-tree-auto-save-history nil)
    (setq undo-tree-visualizer-diff t)))

(use-package vundo :straight t
      :bind ("C-x U" . vundo)
      :config (setq vundo-glyph-alist vundo-unicode-symbols))
(straight-use-package 'prescient)
(straight-use-package 'smex)
(straight-use-package 'project)
(winner-mode 1)

(defun kill-other-buffers ()
  "Kill all other buffers."
  (interactive)
  (mapc 'kill-buffer (delq (current-buffer) (buffer-list))))

(defun kill-dired-buffers ()
  (interactive)
  (mapc (lambda (buffer)
          (when (eq 'dired-mode (buffer-local-value 'major-mode buffer))
            (kill-buffer buffer)))
        (buffer-list)))

(defun copy-file-name-to-clipboard ()
  "Copy the current buffer file name to the clipboard."
  (interactive)
  (let ((filename (if (equal major-mode 'dired-mode)
                      default-directory
                    (buffer-file-name))))
    (when filename
      (kill-new filename)
      (message "Copied buffer file name '%s' to the clipboard." filename))))

(defun clear-undo-tree ()
  (interactive)
  (setq buffer-undo-tree nil))
(global-set-key [(control c) u] 'clear-undo-tree)

(make-directory (expand-file-name "tmp/auto-saves/" user-emacs-directory) t)

(setq auto-save-list-file-prefix (expand-file-name "tmp/auto-saves/sessions/" user-emacs-directory)
      auto-save-file-name-transforms `((".*" ,(expand-file-name "tmp/auto-saves/" user-emacs-directory) t)))
(setq create-lockfiles nil)

(eval-after-load 'term '(term-set-escape-char ?\C-x))

(defmacro rename-modeline (package-name mode new-name)
  `(eval-after-load ,package-name
     '(defadvice ,mode (after rename-modeline activate)
        (setq mode-name ,new-name))))
(rename-modeline "clojure-mode" clojure-mode "clj")
(rename-modeline "paredit-mode" paredit-mode "()")

(defun my-ivy-matcher-descs ()
  "Return a brief description of `ivy--regex-function'."
  (pcase ivy--regex-function
    ('ivy--regex-fuzzy        "fuzzy")
    ('ivy--regex-ignore-order "order")
    ('ivy--regex-plus         "plus")
    (_                        "other")))

(advice-add 'ivy--matcher-desc :override #'my-ivy-matcher-descs)

(defun my-ivy-rotate-builders ()
  "Slide `ivy--regex-function' across `my-ivy-builders'."
  (when my-ivy-builders
    (setq ivy--old-re nil)
    (setq ivy--regex-function
          (or (cadr (memq ivy--regex-function my-ivy-builders))
              (car my-ivy-builders))))))

(add-hook 'clojure-mode-hook 'lsp)
(add-hook 'clojurescript-mode-hook 'lsp)
(add-hook 'clojurec-mode-hook 'lsp)
(setq clojure-toplevel-inside-comment-form t)
(setq clojure-align-forms-automatically t)
(add-to-list 'auto-mode-alist '("\\.selmer\\'" . clojure-mode))
(add-to-list 'auto-mode-alist '("\\.tpl\\'" . clojure-mode))
(add-to-list 'auto-mode-alist '("\\.map\\'" . clojure-mode))
(add-to-list 'auto-mode-alist '("\\.set\\'" . clojure-mode))
(add-to-list 'auto-mode-alist '("\\.vector\\'" . clojure-mode))
(add-to-list 'auto-mode-alist '("\\.repl\\'" . clojure-mode))
(setq cider-repl-pop-to-buffer-on-connect nil)
(setq cider-session-name-template "%j-----%r-----%p")
(setq cider-repl-wrap-history t)
(setq cider-repl-history-size 1000)
(setq cider-repl-history-file "~/.emacs.d/cider-history")
(setq cider-prompt-save-file-on-load nil)
(setq cider-auto-select-error-buffer nil)
(setq cider-repl-use-clojure-font-lock t)
(setq gc-cons-threshold (* 100 1024 1024)
      read-process-output-max (* 1024 1024)
      treemacs-space-between-root-nodes nil
      company-minimum-prefix-length 1)


(defun cider-eval-last-sexp-in-buffer-as-current-region ()
  "Evaluate the expression preceding point and replace it with its result."
  (interactive)
  (let ((last-sexp (cider-last-sexp)))
    (cider-nrepl-sync-request:eval last-sexp)
    (region-beginning)
    (cider-interactive-eval last-sexp
                            (cider-eval-print-handler)
                            nil
                            (cider--nrepl-pr-request-map))
    (region-end)))

(defun cider-tap-last-expression ()
       (interactive)
       (cider-interactive-eval
         (format "(tap> %s)"
                 (cider-last-sexp))))

(defun clerk-show ()
  (interactive)
  (when-let
      ((filename
        (buffer-file-name)))
    (save-buffer)
    (cider-interactive-eval
     (concat "(nextjournal.clerk/show! \"" filename "\")"))))

(add-to-list 'default-frame-alist '(left-fringe . 0))
(add-to-list 'default-frame-alist '(right-fringe . 0))

(defun reload-emacs ()
  "Reload the init file."
  (interactive)
  (load-file "~/.emacs.d/init.el"))


(global-set-key (kbd "C-v") 'backward-char)

;; (defun mount-bare-git-repo (work-tree git-dir)
;;   (interactive)
;;   (add-to-list 'magit-git-global-arguments (format "--work-tree=%s" (expand-file-name work-tree)))
;;   (add-to-list 'magit-git-global-arguments (format "--git-dir=%s" (directory-file-name git-dir))))

;; (defun mount-dtf () (mount-bare-git-repo ".dtf" "/home/pat"))

(defun mount-dtf ()
  (interactive)
  (unless (boundp 'bare-repo-hook?)
  (eval-after-load 'magit
    '(let ((myconf-path (expand-file-name ".dtf")))
       (when (and (file-exists-p myconf-path)
                  (not (file-exists-p ".git")))
         (add-to-list 'magit-git-global-arguments
                      (format "--work-tree=%s"
                              ;; Drop trailing slash.
                              (directory-file-name
                               ;; Get directory part (`dirname`).
                               (file-name-directory myconf-path))))
         (add-to-list 'magit-git-global-arguments
                      (format "--git-dir=%s" myconf-path)))))
  (setq bare-repo-hook? t)))


(add-hook 'json-mode-hook
          (lambda ()
            (make-local-variable 'js-indent-level)
            (setq tab-width 2)
            (setq js-indent-level 2)))

(custom-set-variables
 ;; custom-set-variables was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 '(custom-safe-themes
   '("bbb13492a15c3258f29c21d251da1e62f1abb8bbd492386a673dcfab474186af" default))
 '(warning-suppress-types '((use-package))))
(custom-set-faces
 ;; custom-set-faces was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 )

