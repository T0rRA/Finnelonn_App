﻿using FunnregistreringsAPI.Models;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Newtonsoft.Json;
using System.Net.Http;

namespace FunnregistreringsAPI.DAL
{
    public interface FunnRepositoryInterface
    {
        public Task<bool> RegistrerFunn(InnFunn nyttfunn, String brukernavn);
        public Task<List<Funn>> GetAllUserFunn(String brukernavn, String passord);
        public Task<bool> DeleteFunn(int funnID);
        public Task<bool> EditFunn(Funn f);
        public Task<Funn> GetFunn(String brukernavn, int funnID);
        public Task<HttpResponseMessage> Get([FromBody] Image img);
    }
}
