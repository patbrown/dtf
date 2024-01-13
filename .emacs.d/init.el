(setq inhibit-startup-message t)

(progn
      (tool-bar-mode -1)
      (menu-bar-mode -1)
      (tooltip-mode -1)
      (set-fringe-mode 10)
      (scroll-bar-mode -1))

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



;;; GERNERAL
(global-set-key (kbd "<escape>") 'keyboard-escape-quit)
(defalias 'yes-or-no-p 'y-or-n-p)
(setq-default
 window-combination-resize t
 x-stretch-cursor t)
(unless (display-graphic-p)
  (xterm-mouse-mode 1))
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
(global-set-key (kbd "C-v") 'backward-char)


(defun smarter-move-beginning-of-line (arg)
  "Move point back to indentation of beginning of line.

Move point to the first non-whitespace character on this line.
If point is already there, move to the beginning of the line.
Effectively toggle between the first non-whitespace character and
the beginning of the line.

If ARG is not nil or 1, move forward ARG - 1 lines first.  If
point reaches the beginning or end of the buffer, stop there."
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

(use-package smartrep :straight t :ensure t)

(use-package vimish-fold :straight t
  :init
  (vimish-fold-global-mode 1)
  (global-set-key (kbd "C-c f s") #'vimish-fold)
  (global-set-key (kbd "C-c f d") #'vimish-fold-delete)
  (global-set-key (kbd "C-c f a") #'vimish-fold-unfold-all)
  (global-set-key (kbd "C-c f A") #'vimish-fold-refold-all)
  (global-set-key (kbd "C-c f f") #'vimish-fold-toggle)
  (global-set-key (kbd "C-c f F") #'vimish-fold-toggle-all))

(use-package which-key :straight t :defer t :init (require 'which-key) (which-key-mode))

(use-package yaml-mode :straight t :config (add-hook 'yaml-mode-hook (lambda () (define-key yaml-mode-map "\C-m" 'newline-and-indent))))

(use-package yasnippet :straight t :init (progn (require 'yasnippet) (yas-global-mode 1)))





(use-package lsp-mode :straight t :ensure t
  :hook
  ((clojure-mode . lsp))
  :config
  (lsp-enable-which-key-integration t))


(global-set-key     (kbd "C-c C-c a") 'append-to-register)
(global-set-key     (kbd "C-c C-c i") 'insert-register)
(global-set-key     (kbd "C-c C-c w") 'window-configuration-to-register)
(global-set-key     (kbd "C-c C-c y") 'copy-to-register)
(global-set-key     (kbd "C-x r l")   'list-register)
(global-set-key     (kbd "M-h")               'fzf)
(global-set-key     (kbd "C-i")               'completion-at-point)
(global-set-key     (kbd "C-x C-r")               'counsel-register)
(global-set-key     (kbd "C-c C-x r")         'eval-region)
(global-set-key     (kbd "C-c C-x b")         'eval-buffer)
(global-set-key     (kbd "RET")               'newline-and-indent)
(global-set-key     (kbd "C-j")               'newline-and-indent)
(global-set-key     (kbd "C-l")               'goto-line)
(global-set-key     (kbd "M-;")               'comment-dwim)
(global-set-key     (kbd "C-c a")               'beginning-of-buffer)
(global-set-key (kbd "C-t") 'forward-paragraph)
(global-set-key (kbd "C-r") 'backward-paragraph)
(global-set-key     (kbd "C-c e")               'end-of-buffer)
(global-set-key     (kbd "C-h C-f")           'find-function)
(global-set-key     (kbd "C-h C-k")           'find-function-on-key)
(global-set-key     (kbd "C-h C-v")           'find-variable)
(global-set-key     (kbd "C-h C-l")           'find-library)
(global-set-key     (kbd "C-SPC")             'set-mark-command)
(global-set-key (kbd "C-x |")                 'toggle-window-split)
(global-set-key (kbd "C-c w")                 'rotate-windows)
(global-set-key [remap move-beginning-of-line]
                'smarter-move-beginning-of-line)
(global-set-key (kbd "C-c C-w")               'kill-ring-save)
(global-set-key (kbd "C-c C-p")               'ace-window)
(global-set-key (kbd "S-C-<right>")           'paredit-forward-slurp-sexp)
(global-set-key (kbd "S-C-<left>")            'paredit-backward-slurp-sexp)
(global-set-key (kbd "S-C-<up>")              'paredit-backward)
(global-set-key (kbd "S-C-<down>")            'paredit-forward)
(global-set-key (kbd "C-c C-j")               'jump-to-register)
(global-set-key (kbd "C-c C-x C-k")           'browse-kill-ring)
(global-set-key (kbd "M-x")                   'counsel-M-x)
;(global-set-key (kbd "C-c g")                 'counsel-git)
(global-set-key (kbd "C-c G")                 'counsel-git-grep)
(global-set-key (kbd "C-x b") 'ivy-switch-buffer)
(global-set-key (kbd "C-x f") 'projectile-find-file)
;; YASNIPPETS
(global-set-key (kbd "C-x C-f") 'find-file)
(global-set-key (kbd "C-c C-f") 'projectile-find-file-dwim)
(global-set-key (kbd "C-c u") 'swiper-all)
(global-set-key (kbd "C-x C-d") 'dirvish-dired)
(global-set-key (kbd "C-c C-f") 'counsel-projectile-find-file)
(global-set-key (kbd "C-x <") 'previous-buffer)
(global-set-key (kbd "C-x >") 'next-buffer)
(global-set-key (kbd "C-c r") 'cider-eval-last-sexp-in-buffer-as-current-region)
(global-set-key (kbd "C-c s") 'cider-eval-sexp-at-point)
(global-set-key (kbd "C-c t") 'cider-tap-last-expression)
(global-set-key (kbd "C-c <") 'lsp-ui-find-prev-reference)
(global-set-key (kbd "C-c d") 'lsp-find-definition)
(global-set-key (kbd "C-c C-c r") 'lsp-find-references)
(global-set-key (kbd "C-<up>") 'windmove-up)
(global-set-key (kbd "C-<down>") 'windmove-down)
(global-set-key (kbd "C-c v") 'indent-sexp)
(global-set-key (kbd "M-u") 'delete-indentation)
(global-set-key (kbd "M-U") 'indent-region)

;; # KEYMAP SYSTEM
(global-unset-key (kbd "C-c c"))
(define-prefix-command 'start-map)
(global-set-key (kbd "C-c c") 'start-map)
(define-key start-map (kbd "+") 'help)

(global-unset-key (kbd "C-c c"))
(define-prefix-command 'start-map)
(define-prefix-command 'clj-map)
(define-prefix-command 'lisp-map)
(define-prefix-command 'jump-map)
(define-prefix-command 'tab-map)
(define-prefix-command 'z-map)
(define-prefix-command 'o-map)
(define-prefix-command 'v-map)
(global-set-key (kbd "C-c c") 'start-map)
(define-key start-map (kbd "b") 'bufler-list)
(define-key start-map (kbd "c") 'clj-map)
(define-key start-map (kbd "e") 'eval-buffer)
(define-key start-map (kbd "a") 'winner-undo)
(define-key start-map (kbd "s") 'winner-redo)
(define-key start-map (kbd "d") 'windmove-left)
(define-key start-map (kbd "f") 'windmove-right)
(define-key start-map (kbd "j") 'jump-map)
(define-key start-map (kbd "l") 'lisp-map)
(define-key start-map (kbd "n") 'tab-line-switch-to-next-tab)
(define-key start-map (kbd "p") 'tab-line-switch-to-prev-tab)
(define-key start-map (kbd "u") 'swiper-all)
(define-key start-map (kbd "w") 'burly-open-bookmark)
(define-key start-map (kbd "x") 'delete-indentation)
(define-key start-map (kbd "z") 'z-map)

(smartrep-define-key
    global-map "C-c ;" '(("f" . 'windmove-up)
			 ("d" . 'windmove-down)
			 ("s" . 'windmove-right)
			 ("a" . 'windmove-down)
			 ("g" . 'bufler-list)
			 ("h" . 'buf-move)
			 ("j" . 'buf-move-up)
			 ("k" . 'buf-move-down)
			 ("l" . 'buf-move-left)
			 (";" . 'buf-move-right)
			 ("n" . 'winner-redo)
			 ("p" . 'winner-undo)
			 ("b" . 'ivy-switch-buffer)
			 ("x" . 'kill-buffer-and-window)
			 ("SPC" . 'jump-map)
			 ))

(smartrep-define-key
    global-map "C-c x" '(("x" . 'delete-indentation)))

;; (smartrep-define-key
;;     global-map "C-c t" '(("n" . 'tab-line-switch-to-next-tab)
;; 		       ("p" . 'tab-line-switch-to-prev-tab)
;; 		       ("x" . 'kill-buffer)
;; 		       ("f" . 'tab-next)
;; 		       ("b" . 'tab-previous)
;; 		       ("r" . 'tab-rename)
;; 		       ("X" . 'tab-close)
;; 		       ("k" . 'kill-buffer)))

;; Jump Map
(global-set-key (kbd "C-c j") 'jump-map)
(define-key jump-map (kbd "a") 'beginning-of-buffer)
(define-key jump-map (kbd "b") 'burly-open-bookmark)
(define-key jump-map (kbd "e") 'end-of-buffer)
(define-key jump-map (kbd "f") 'ace-window)
(define-key jump-map (kbd "c") 'ace-jump-char-mode)
(define-key jump-map (kbd "l") 'ace-jump-line-mode)
(define-key jump-map (kbd "n") 'tab-line-switch-to-next-tab)
(define-key jump-map (kbd "p") 'tab-line-switch-to-prev-tab)
(define-key jump-map (kbd "j") 'jump-to-register)
(define-key jump-map (kbd "w") 'ace-jump-word-mode)
(define-key jump-map (kbd "x") 'tab-line-close-tab)
(define-key jump-map (kbd "z") 'ace-jump-zap-up-to-char)

;; Lisp Map mostly paredit
(global-set-key (kbd "C-c l") 'lisp-map)
(define-key lisp-map (kbd ";") 'paredit-forward-slurp-sexp)
(define-key lisp-map (kbd "l") 'paredit-backward-slurp-sexp)
(define-key lisp-map (kbd "k") 'paredit-forward-barf-sexp)
(define-key lisp-map (kbd "j") 'paredit-backward-barf-sexp)
(define-key lisp-map (kbd "r") 'paredit-wrap-round)
(define-key lisp-map (kbd "c") 'paredit-wrap-curly)
(define-key lisp-map (kbd "s") 'paredit-wrap-square)
    (smartrep-define-key
        global-map "M-p" '((";" . 'paredit-forward-slurp-sexp)
                           ("l" . 'paredit-backward-slurp-sexp)
                           ("k" . 'paredit-forward-barf-sexp)
                           ("j" . 'paredit-backward-barf-sexp)
                           ("r" . 'paredit-wrap-round)
			   ("c" . 'paredit-wrap-curly)
			   ("s" . 'paredit-wrap-square)))


;; Dope reigsters, technomancy.
(defvar refile-map (make-sparse-keymap))
(setq find-file-visit-truename t)

(defmacro defregister (key file)
  `(progn
     (set-register ,key (cons 'file ,file))
     (define-key refile-map
       (char-to-string ,key)
       (lambda (prefix)
         (interactive "p")
         (let ((org-refile-targets '(((,file) :maxlevel . 6)))
               (current-prefix-arg (or current-prefix-arg '(4))))
           (call-interactively 'org-refile))))))

(defregister ?a "~/.ssh")

;; Buffer Navigation
(setq ns-pop-up-frames nil)
(size-indication-mode t)
(transient-mark-mode 1)
(setq scroll-margin 0
      scroll-conservatively 100000
      scroll-preserve-screen-position 1)
(fset 'yes-or-no-p 'y-or-n-p)
(setq vc-follow-symlinks t)

(when (< 26 emacs-major-version)
  (tab-bar-mode 1)		      ;; enable tab bar
  (setq tab-bar-show 1)		      ;; hide bar if <= 1 tabs open
  (setq tab-bar-close-button-show nil) ;; hide tab close / X button
  (setq tab-bar-new-button-show nil)   ;; hide tab close / X button
  (setq tab-bar-new-tab-choice "*scratch*") ;; buffer to show in new tabs
  (setq tab-bar-tab-hints t)		      ;; show tab numbers
  (setq tab-bar-format '(tab-bar-format-tabs tab-bar-separator)))
                                            ;; elements to include in bar

;; General Look and Feel
(setq dired-use-ls-dired nil)
(setq display-time-24hr-format t)
(setq display-time-load-average t)
(display-time)
(if (fboundp 'fringe-mode)
    (fringe-mode -1))
(display-line-numbers-mode 1)
(setq linum-format "%d  ")
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


(use-package ace-jump-mode
  :straight t
  :init
  (autoload 'ace-jump-mode "ace-jump-mode" "Emacs quick move minor mode" t)
  ;(define-key global-map (kbd "C-c SPC") 'ace-jump-mode)
  (setq ace-jump-mode-gray-background nil)
  (setq ace-jump-mode-move-keys
        '(?j ?f ?k ?d ?l ?s ?\; ?a ?h ?g ?u ?r ?i ?e ?o ?w ?p ?q ?y ?t ?m ?v ?, ?c ?. ?x ?/ ?z ?n ?b ?7 ?4 ?8 ?3 ?9 ?2 ?0 ?1 ?6 ?5)))

(winner-mode 1)

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
          (funcall fn)))))
)
  (global-set-key (kbd "C-c <left>") 'windmove-left)
  (global-set-key (kbd "C-c <right>") 'windmove-right)
  (global-set-key (kbd "C-c <up>")   'windmove-up)
  (global-set-key (kbd "C-c <down>") 'windmove-down)


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

(eval-after-load 'term
  '(term-set-escape-char ?\C-x))

(defmacro rename-modeline (package-name mode new-name)
  `(eval-after-load ,package-name
     '(defadvice ,mode (after rename-modeline activate)
        (setq mode-name ,new-name))))
(rename-modeline "clojure-mode" clojure-mode "clj")
(rename-modeline "paredit-mode" paredit-mode "()")



(straight-use-package 'prescient)
(straight-use-package 'ivy-prescient)
(straight-use-package 'smex)

(use-package ivy
  :straight t
  :defer 0.1
  :diminish
  :bind (("C-c C-r" . ivy-resume))
  :custom
  (ivy-count-format "(%d/%d) ")
  (ivy-use-virtual-buffers t)
  :init
  (ivy-prescient-mode)
  (defvar my-ivy-builders '(ivy--regex-ignore-order
                            ivy--regex-fuzzy
			    ivy--regex-plus)
  "Preferred values for `ivy--regex-function'.")

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
              (car my-ivy-builders)))))

(advice-add 'ivy-toggle-fuzzy :override #'my-rotate-builders))

(use-package ivy-rich
  :straight t
  :after counsel
  :init (setq ivy-rich-path-style 'abbrev
              ivy-virtual-abbreviate 'full)
  :config
  (ivy-mode)
  (ivy-rich-mode))

(use-package counsel
  :straight t
  :after ivy
  :config (counsel-mode))

(use-package swiper
  :straight t
  :after ivy
  :bind (("C-s" . swiper)))

(use-package cider
  :straight t
  :ensure t)

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


(use-package multiple-cursors
  :straight t
  :defer t
  :bind (("C-C M->" . mc/mark-next-like-this)
         ("C-c M-<" . mc/mark-previous-like-this)
         ("C-c M-!" . mc/mark-all-like-this)
         ("C-c C-c M-!" . mc/edit-lines)
	 ))

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



(add-to-list 'default-frame-alist '(left-fringe . 0))
(add-to-list 'default-frame-alist '(right-fringe . 0))
(global-unset-key (kbd "<left>"))
(global-unset-key (kbd "<right>"))
(global-unset-key (kbd "<up>"))
(global-unset-key (kbd "<down>"))
;(global-unset-key (kbd "C-o"))
(global-unset-key (kbd "C-v"))
(global-unset-key (kbd "M-s"))
(defun reload-emacs ()
  "Reload the init file."
  (interactive)
  (load-file "~/.emacs.d/init.el"))

(use-package spacemacs-theme
  :straight t
  :ensure t
  :init (load-theme 'spacemacs-dark t))
(use-package transient :straight t)
(use-package company
  :straight t)
(straight-use-package 'project)
(global-set-key (kbd "C-v") 'backward-char)
