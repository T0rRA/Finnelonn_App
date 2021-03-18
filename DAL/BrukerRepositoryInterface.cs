﻿using FunnregistreringsAPI.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace FunnregistreringsAPI.DAL
{
    public interface BrukerRepositoryInterface
    {
        public Task<bool> AttemptLogin(InnBruker bruker);

        public Task<bool> CreateUser(InnBruker bruker);

        public Task<bool> SendPwResetLink(String epost);

        public Task<bool> ChangePassword(InnBruker bruker, String token, string newPassword, string newPassword2);
    }
}